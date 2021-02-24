package org.bahmni_avni_integration.worker.bahmni;

import org.bahmni_avni_integration.client.OpenMRSWebClient;
import org.bahmni_avni_integration.client.bahmni.ClientCookies;
import org.bahmni_avni_integration.config.OpenMRSAtomFeedPropertiesFactory;
import org.bahmni_avni_integration.domain.Constants;
import org.bahmni_avni_integration.worker.bahmni.atomfeedworker.OpenMrsPatientEventWorker;
import org.ict4h.atomfeed.client.AtomFeedProperties;
import org.ict4h.atomfeed.client.repository.AllFeeds;
import org.ict4h.atomfeed.client.repository.jdbc.AllFailedEventsJdbcImpl;
import org.ict4h.atomfeed.client.repository.jdbc.AllMarkersJdbcImpl;
import org.ict4h.atomfeed.client.service.AtomFeedClient;
import org.ict4h.atomfeed.server.transaction.AtomFeedSpringTransactionSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class BahmniEncounterWorker {
    private final AtomFeedProperties feedProperties;
    private final AllMarkersJdbcImpl allMarkers;
    private final AllFailedEventsJdbcImpl allFailedEvents;
    private final AtomFeedSpringTransactionSupport transactionManagerImpl;
    private OpenMRSWebClient openMRSWebClient;
    @Value("${bahmni.feed.patient}")
    private String patientFeedLink;
    @Autowired
    private OpenMrsPatientEventWorker eventWorker;

    @Autowired
    public BahmniEncounterWorker(PlatformTransactionManager transactionManager, DataSource dataSource, OpenMRSWebClient openMRSWebClient, OpenMRSAtomFeedPropertiesFactory atomFeedPropertiesFactory) {
        this.openMRSWebClient = openMRSWebClient;
        feedProperties = atomFeedPropertiesFactory.getProperties();
        transactionManagerImpl = new AtomFeedSpringTransactionSupport(
                transactionManager,
                dataSource
        );
        allMarkers = new AllMarkersJdbcImpl(transactionManagerImpl);
        allFailedEvents = new AllFailedEventsJdbcImpl(transactionManagerImpl);
    }

    public void processEncounters(Constants constants) {
        try {
            eventWorker.setConstants(constants);
            URI uri = new URI(patientFeedLink);
            ClientCookies cookies = openMRSWebClient.getCookies();
            AllFeeds allFeeds = new AllFeeds(feedProperties, cookies);
            AtomFeedClient atomFeedClient = new AtomFeedClient(
                    allFeeds,
                    allMarkers,
                    allFailedEvents,
                    feedProperties,
                    transactionManagerImpl,
                    uri,
                    eventWorker);
            atomFeedClient.processEvents();
        } catch (URISyntaxException e) {
            throw new RuntimeException("error for uri:" + patientFeedLink, e);
        }
    }
}