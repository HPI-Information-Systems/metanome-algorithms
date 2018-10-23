package de.hpi.is.md.hybrid.impl.validation;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.hybrid.Rhs;
import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.Validator;
import de.hpi.is.md.hybrid.impl.validation.arbitrary.ArbitraryValidationTaskFactory;
import de.hpi.is.md.hybrid.impl.validation.empty.EmptyValidationTaskFactory;
import de.hpi.is.md.hybrid.impl.validation.single.SingleValidationTaskFactory;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import java.util.Collection;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
//@Metrics
@Slf4j
public class ValidatorImpl implements Validator {

	@NonNull
	private final ArbitraryValidationTaskFactory arbitraryFactory;
	@NonNull
	private final EmptyValidationTaskFactory emptyFactory;
	@NonNull
	private final SingleValidationTaskFactory singleFactory;

	public static ValidatorBuilder builder() {
		return new ValidatorBuilder();
	}

	@Override
	@Timed
	public ValidationResult validate(MDSite lhs, Collection<Rhs> rhs) {
		log.trace("Will validate {} RHSs for {}: {}", Integer.valueOf(rhs.size()), lhs,
			StreamUtils.seq(rhs).toString(","));
		ValidationTask task = createTask(lhs, rhs);
		return task.validate();
	}

	private ValidationTask createTask(MDSite lhs, Iterable<Rhs> rhs) {
		if (lhs.cardinality() == 0) {
			return emptyFactory.createTask(lhs, rhs);
		}
		if (lhs.cardinality() == 1) {
			return singleFactory.createTask(lhs, rhs);
		}
		return arbitraryFactory.createTask(lhs, rhs);
	}

}
