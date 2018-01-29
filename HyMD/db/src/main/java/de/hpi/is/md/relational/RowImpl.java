package de.hpi.is.md.relational;


import de.hpi.is.md.util.BetterMap;
import de.hpi.is.md.util.BetterMapDecorator;
import de.hpi.is.md.util.CastUtils;
import java.util.Map;
import java.util.Optional;
import lombok.ToString;

@ToString
final class RowImpl extends AbstractRow {

	private final BetterMap<Column<?>, Object> values;

	private RowImpl(Schema schema, Map<Column<?>, Object> values) {
		super(schema);
		this.values = new BetterMapDecorator<>(values);
	}

	static Row create(Schema schema, Map<Column<?>, Object> values) {
		return new RowImpl(schema, values);
	}

	@Override
	public <T> Optional<T> get(Column<T> column) {
		return values.get(column)
			.map(CastUtils::as);
	}

}
