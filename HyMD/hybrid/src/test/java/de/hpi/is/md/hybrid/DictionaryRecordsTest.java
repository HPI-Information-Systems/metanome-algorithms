package de.hpi.is.md.hybrid;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.junit.Test;

public abstract class DictionaryRecordsTest {

	@Test
	public void testGet() {
		DictionaryRecords records = createRecords();
		assertThat(records.get(0)).isEqualTo(new int[]{0, 0, 0});
		assertThat(records.get(1)).isEqualTo(new int[]{1, 2, 3});
		assertThat(records.get(2)).isEqualTo(new int[]{3, 2, 1});
	}

	@Test
	public void testGetAll() {
		DictionaryRecords records = createRecords();
		assertThat(records.getAll()).hasSize(3);
		assertThat(records.getAll()).contains(0, 1, 2);
	}

	@Test
	public void testIteration() {
		DictionaryRecords records = createRecords();
		assertThat(records).hasSize(3);
		assertThat(records).contains(new int[]{0, 0, 0});
		assertThat(records).contains(new int[]{1, 2, 3});
		assertThat(records).contains(new int[]{3, 2, 1});
	}

	protected abstract DictionaryRecords createRecords(Collection<int[]> records);

	private DictionaryRecords createRecords() {
		Collection<int[]> records = ImmutableList.<int[]>builder()
			.add(new int[]{0, 0, 0})
			.add(new int[]{1, 2, 3})
			.add(new int[]{3, 2, 1})
			.build();
		return createRecords(records);
	}

}
