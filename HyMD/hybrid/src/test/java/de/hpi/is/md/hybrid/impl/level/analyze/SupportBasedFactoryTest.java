package de.hpi.is.md.hybrid.impl.level.analyze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.ValidationResult.LhsResult;
import de.hpi.is.md.hybrid.impl.level.analyze.AnalyzeStrategy.Factory;
import de.hpi.is.md.hybrid.md.MDSite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SupportBasedFactoryTest {

	@Mock
	private Factory supportedFactory;
	@Mock
	private Factory notSupportedFactory;

	@Test
	public void testNotSupported() {
		Factory factory = createFactory();
		assertThat(factory.create(new LhsResult(Mockito.mock(MDSite.class), 9)))
			.isInstanceOf(NotSupportedStrategy.class);
	}

	@Test
	public void testSupported() {
		Factory factory = createFactory();
		assertThat(factory.create(new LhsResult(Mockito.mock(MDSite.class), 10)))
			.isInstanceOf(SupportedStrategy.class);
	}

	private Factory createFactory() {
		when(notSupportedFactory.create(any()))
			.thenReturn(Mockito.mock(NotSupportedStrategy.class));
		when(supportedFactory.create(any())).thenReturn(Mockito.mock(SupportedStrategy.class));
		return SupportBasedFactory.builder()
			.minSupport(10)
			.notSupportedFactory(notSupportedFactory)
			.supportedFactory(supportedFactory)
			.build();
	}

}