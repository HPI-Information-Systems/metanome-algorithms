package de.hpi.is.md.hybrid.impl.preprocessed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.hpi.is.md.hybrid.DictionaryRecords;
import de.hpi.is.md.hybrid.PositionListIndex;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.InputCloseException;
import de.hpi.is.md.relational.InputOpenException;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.relational.RelationalInput;
import de.hpi.is.md.relational.Row;
import de.hpi.is.md.relational.Schema;
import de.hpi.is.md.util.Dictionary;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class CompressorTest {

	private static final Column<String> A = Column.of("a", String.class);
	private static final Column<Integer> B = Column.of("b", Integer.class);

	@Mock
	private Relation relation;
	@Mock
	private RelationalInput input;

	@Test
	public void testDictionaryA() {
		mock();
		CompressedRelation compressed = compress();
		CompressedColumn<String> column = compressed.getColumn(0);
		Dictionary<String> dictionary = column.getDictionary();
		assertThat(dictionary.values()).hasSize(3);
		assertThat(dictionary.values()).contains("foo");
		assertThat(dictionary.values()).contains("bar");
		assertThat(dictionary.values()).contains("baz");
		assertThat(dictionary.getOrAdd("foo")).isEqualTo(0);
		assertThat(dictionary.getOrAdd("bar")).isEqualTo(1);
		assertThat(dictionary.getOrAdd("baz")).isEqualTo(2);
	}

	@Test
	public void testDictionaryB() {
		mock();
		CompressedRelation compressed = compress();
		CompressedColumn<Integer> column = compressed.getColumn(1);
		Dictionary<Integer> dictionary = column.getDictionary();
		assertThat(dictionary.values()).hasSize(2);
		assertThat(dictionary.values()).contains(2);
		assertThat(dictionary.values()).contains(1);
		assertThat(dictionary.getOrAdd(2)).isEqualTo(0);
		assertThat(dictionary.getOrAdd(1)).isEqualTo(1);
	}

	@Test
	public void testDictionaryRecords() {
		mock();
		CompressedRelation compressed = compress();
		DictionaryRecords dictionaryRecords = compressed.getDictionaryRecords();
		assertThat(dictionaryRecords.getAll()).hasSize(3);
		assertThat(dictionaryRecords.get(0).length).isEqualTo(2);
		assertThat(dictionaryRecords.get(1)[1]).isNotEqualTo(dictionaryRecords.get(0)[1]);
		assertThat(dictionaryRecords.get(1)[1]).isEqualTo(dictionaryRecords.get(2)[1]);
	}

	@Test
	public void testIndexOf() {
		mock();
		CompressedRelation compressed = compress();
		assertThat(compressed.indexOf(A)).isEqualTo(0);
		assertThat(compressed.indexOf(B)).isEqualTo(1);
	}

	@Test(expected = RuntimeException.class)
	public void testInputCloseException() throws InputCloseException {
		doThrow(InputCloseException.class).when(input).close();
		mock();
		compress();
		fail();
	}

	@Test(expected = RuntimeException.class)
	public void testInputOpenException() throws InputOpenException {
		doThrow(InputOpenException.class).when(relation).open();
		compress();
		fail();
	}

	@Test
	public void testPliA() {
		mock();
		CompressedRelation compressed = compress();
		CompressedColumn<String> column = compressed.getColumn(0);
		PositionListIndex pli = column.getPli();
		assertThat(pli.get(0)).hasSize(1);
		assertThat(pli.get(1)).hasSize(1);
		assertThat(pli.get(2)).hasSize(1);
		assertThat(pli.get(0)).contains(0);
		assertThat(pli.get(1)).contains(1);
		assertThat(pli.get(2)).contains(2);
	}

	@Test
	public void testPliB() {
		mock();
		CompressedRelation compressed = compress();
		CompressedColumn<Integer> column = compressed.getColumn(1);
		PositionListIndex pli = column.getPli();
		assertThat(pli.get(0)).hasSize(1);
		assertThat(pli.get(1)).hasSize(2);
		assertThat(pli.get(0)).contains(0);
		assertThat(pli.get(1)).contains(1);
		assertThat(pli.get(1)).contains(2);
	}

	private CompressedRelation compress() {
		return Compressor.builder()
			.records(MapDictionaryRecords.builder())
			.compress(relation);
	}

	private void mock() {
		Schema schema = Schema.of(Arrays.asList(A, B));
		Collection<Row> rows = ImmutableList.<Row>builder()
			.add(Row.create(schema, ImmutableMap.of(A, "foo", B, 2)))
			.add(Row.create(schema, ImmutableMap.of(A, "bar", B, 1)))
			.add(Row.create(schema, ImmutableMap.of(A, "baz", B, 1)))
			.build();
		mockRelation(schema, rows);
	}

	private void mockRelation(Schema schema, Iterable<Row> rows) {
		try {
			when(relation.open()).thenReturn(input);
		} catch (InputOpenException e) {
			throw new RuntimeException(e);
		}
		when(input.iterator()).thenReturn(rows.iterator());
		doCallRealMethod().when(input).forEach(any());
		when(input.getSchema()).thenReturn(schema);
	}

}