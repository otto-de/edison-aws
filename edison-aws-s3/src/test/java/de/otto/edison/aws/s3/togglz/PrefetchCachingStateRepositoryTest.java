package de.otto.edison.aws.s3.togglz;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class PrefetchCachingStateRepositoryTest {

    private PrefetchCachingStateRepository prefetchCachingStateRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private Feature feature;

    @Mock
    private FeatureState featureState;
    private AutoCloseable mocks;

    @BeforeEach
    public void setUp() {
        mocks = openMocks(this);
        prefetchCachingStateRepository = new PrefetchCachingStateRepository(stateRepository);
        when(feature.name()).thenReturn("someToggleName");
    }

    @AfterEach
    public void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    public void shouldFetchInitialTogglzStatefromDelegateAndServeSubsequentRequestsFromCache() {
        // given
        when(stateRepository.getFeatureState(feature)).thenReturn(featureState);

        // when
        prefetchCachingStateRepository.getFeatureState(feature);
        prefetchCachingStateRepository.getFeatureState(feature);
        prefetchCachingStateRepository.getFeatureState(feature);

        // then
        verify(stateRepository, times(1)).getFeatureState(feature);
    }

    @Test
    public void shouldSetFeatureStateAndPutItIntoCache() {
        // given
        when(featureState.getFeature()).thenReturn(feature);
        // when
        prefetchCachingStateRepository.setFeatureState(featureState);
        verify(stateRepository, times(1)).setFeatureState(featureState);

        final FeatureState featureStateFromCache = prefetchCachingStateRepository.getFeatureState(feature);
        assertThat(featureStateFromCache, is(featureState));
        verify(stateRepository, never());
    }
}