package net.ripe.db.whois.common.query.integration;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.query.QueryMessages;
import net.ripe.db.whois.common.query.VersionDateTime;
import net.ripe.db.whois.common.query.domain.QueryException;
import net.ripe.db.whois.common.query.executor.CaptureResponseHandler;
import net.ripe.db.whois.common.query.executor.VersionQueryExecutor;
import net.ripe.db.whois.common.query.query.Query;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static net.ripe.db.whois.common.query.support.PatternMatcher.matchesPattern;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

// TODO: [ES] refactor
@RunWith(MockitoJUnitRunner.class)
public class VersionQueryExecutorTestIntegration {
    @Mock VersionInfo versionInfo1;
    @Mock VersionInfo versionInfo2;
    @Mock VersionInfo versionInfo3;
    @Mock VersionInfo versionInfo4;
    @Mock SourceContext sourceContext;
    @Mock QueryMessages queryMessages;
    @Mock VersionDao versionDao;
    @InjectMocks VersionQueryExecutor subject;

    @Before
    public void setup() {
        when(sourceContext.getCurrentSource()).thenReturn(Source.master("TEST"));
        when(queryMessages.versionListStart(any(CharSequence.class), any(CharSequence.class))).thenReturn(new Message(Messages.Type.INFO, ""));
        when(queryMessages.versionPersonRole(any(CharSequence.class), any(CharSequence.class))).thenReturn(new Message(Messages.Type.INFO, ""));
        when(queryMessages.noResults(any(CharSequence.class))).thenReturn(new Message(Messages.Type.ERROR, ""));
        when(queryMessages.versionDeleted(any(CharSequence.class))).thenReturn(new Message(Messages.Type.INFO, ""));
        when(queryMessages.malformedQuery(any(String.class))).thenAnswer(new Answer<Message>() {
            @Override
            public Message answer(InvocationOnMock invocation) throws Throwable {
                return new Message(Messages.Type.ERROR, invocation.getArguments()[0].toString());
            }
        });
        when(queryMessages.versionOutOfRange(any(Integer.class))).thenReturn(new Message(Messages.Type.ERROR, ""));

    }

    @Test
    public void supportTest() {
        assertThat(subject.supports(new Query("10.0.0.0", Query.Origin.LEGACY, false, queryMessages)), is(false));
        assertThat(subject.supports(new Query("--list-versions 10.0.0.0", Query.Origin.LEGACY, false, queryMessages)), is(true));
        assertThat(subject.supports(new Query("--show-version 2 10.0.0.0", Query.Origin.LEGACY, false, queryMessages)), is(true));
    }

    @Test
    public void notFoundList() {
        when(versionDao.findByKey(ObjectType.IRT, "IRT-THISONE")).thenReturn(null);

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(new Query("--list-versions IRT-THISONE", Query.Origin.LEGACY, false, queryMessages), responseHandler);

        assertThat(responseHandler.getResponseObjects(), hasSize(1));
    }

    @Test
    public void goodListResponse() {
        setupVersionMock(versionInfo1, 1, 1312210585L);
        setupVersionMock(versionInfo2, 2, 1334066282L);
        setupVersionMock(versionInfo3, 3, 1335336916L);

        final VersionLookupResult as2050 = new VersionLookupResult(Lists.newArrayList(versionInfo1, versionInfo2, versionInfo3), ObjectType.AUT_NUM, "AS2050");
        when(versionDao.findByKey(ObjectType.AUT_NUM, "AS2050")).thenReturn(as2050);
        when(versionDao.getObjectType("AS2050")).thenReturn(ImmutableSet.of(ObjectType.AUT_NUM));

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(new Query("--list-versions AS2050", Query.Origin.LEGACY, false, queryMessages), responseHandler);

        Iterator<? extends ResponseObject> result = responseHandler.getResponseObjects().iterator();
        assertThat(result.next().toString(), containsString(queryMessages.versionListStart("AUT-NUM", "AS2050").toString()));

        assertThat(result.next().toString(), matchesPattern("rev#\\s+Date\\s+Op.*"));

        assertThat(result.next().toString(), matchesPattern("1\\s+2011-08-01 14:56\\s+ADD/UPD"));
        assertThat(result.next().toString(), matchesPattern("2\\s+2012-04-10 13:58\\s+ADD/UPD"));
        assertThat(result.next().toString(), matchesPattern("3\\s+2012-04-25 06:55\\s+ADD/UPD"));

        assertThat(result.next().toString(), is(""));
        assertThat(result.hasNext(), is(false));
    }

    @Test
    public void listVersions_deleted() {
        setupVersionMock(versionInfo1, 1, 1312210585L);
        setupVersionMock(versionInfo2, 2, 1334066282L);
        setupVersionMock(versionInfo3, 3, 1335336916L);
        when(versionInfo3.getOperation()).thenReturn(Operation.DELETE);

        final VersionLookupResult as2050 = new VersionLookupResult(Lists.newArrayList(versionInfo1, versionInfo2, versionInfo3), ObjectType.AUT_NUM, "AS2050");
        when(versionDao.findByKey(ObjectType.AUT_NUM, "AS2050")).thenReturn(as2050);
        when(versionDao.getObjectType("AS2050")).thenReturn(Collections.singleton(ObjectType.AUT_NUM));

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(new Query("--list-versions AS2050", Query.Origin.LEGACY, false, queryMessages), responseHandler);

        final List<ResponseObject> responseObjects = responseHandler.getResponseObjects();
        assertThat(new String(responseObjects.get(0).toByteArray()), is(queryMessages.versionListStart(ObjectType.AUT_NUM.getName().toUpperCase(), "AS2050").toString()));
        assertThat(new String(responseObjects.get(1).toByteArray()), is(queryMessages.versionDeleted("2012-04-25 06:55").toString()));
    }

    @Test
    public void showInfo_deleted() {
        when(versionDao.getObjectType("AS2050")).thenReturn(Collections.singleton(ObjectType.AUT_NUM));

        setupVersionMock(versionInfo1, 1, 1312210585L);
        setupVersionMock(versionInfo2, 2, 1334066282L);
        when(versionInfo2.getOperation()).thenReturn(Operation.DELETE);

        final VersionLookupResult as2050 = new VersionLookupResult(Lists.newArrayList(versionInfo1, versionInfo2), ObjectType.AUT_NUM, "AS2050");
        when(versionDao.findByKey(ObjectType.AUT_NUM, "AS2050")).thenReturn(as2050);

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(new Query("--show-version 1 AS2050", Query.Origin.LEGACY, false, queryMessages), responseHandler);

        final List<ResponseObject> responseObjects = responseHandler.getResponseObjects();
        assertThat(new String(responseObjects.get(0).toByteArray()), is(queryMessages.versionListStart(ObjectType.AUT_NUM.getName().toUpperCase(), "AS2050").toString()));
        assertThat(new String(responseObjects.get(1).toByteArray()), is(queryMessages.versionDeleted("2012-04-10 13:58").toString()));
    }

    @Test
    public void showInfo_version_too_high() {
        when(versionDao.getObjectType("AS2050")).thenReturn(Collections.singleton(ObjectType.AUT_NUM));
        setupVersionMock(versionInfo1, 1, 1312210585L);
        final VersionLookupResult as2050 = new VersionLookupResult(Lists.newArrayList(versionInfo1), ObjectType.AUT_NUM, "AS2050");
        when(versionDao.findByKey(ObjectType.AUT_NUM, "AS2050")).thenReturn(as2050);

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(new Query("--show-version 2 AS2050", Query.Origin.LEGACY, false, queryMessages), responseHandler);

        final Iterator<? extends ResponseObject> iterator = responseHandler.getResponseObjects().iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(new String(iterator.next().toByteArray()), is(queryMessages.versionOutOfRange(1).toString()));
    }

    @Test
    public void showInfo_version_too_low() {
        try {
            final Query response = new Query("--show-version 0 AS2050", Query.Origin.LEGACY, false, queryMessages);
            response.getObjectVersion();
            fail("expected query exception as --show-version 0 is not allowed");
        } catch (QueryException e) {
            assertThat(e.getMessage(), containsString("version flag number must be greater than 0"));
        }
    }

    @Test
    public void listVersions_person_role() {
        when(versionDao.getObjectType("TP1-TEST")).thenReturn(Collections.singleton(ObjectType.ROLE));
        setupVersionMock(versionInfo1, 1, 1312210585L);
        setupVersionMock(versionInfo2, 2, 1334066282L);

        final VersionLookupResult tp1 = new VersionLookupResult(Lists.newArrayList(versionInfo1, versionInfo2), ObjectType.ROLE, "TP1-TEST");
        when(versionDao.findByKey(ObjectType.ROLE, "TP1-TEST")).thenReturn(tp1);

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(new Query("--list-versions TP1-TEST", Query.Origin.LEGACY, false, queryMessages), responseHandler);

        final Iterator<ResponseObject> iterator = responseHandler.getResponseObjects().iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(new String(iterator.next().toByteArray()), is(queryMessages.versionPersonRole("ROLE", "TP1-TEST").toString()));
    }

    @Test
    public void listVersions_person_maintainer() {
        when(versionDao.getObjectType("TP1-TEST")).thenReturn(Sets.immutableEnumSet(ObjectType.PERSON, ObjectType.MNTNER));
        setupVersionMock(versionInfo1, 1, 1312210585L);
        setupVersionMock(versionInfo2, 2, 1334066282L);
        setupVersionMock(versionInfo3, 1, 1334066292L);
        final VersionLookupResult versionLookupResultPerson = new VersionLookupResult(Lists.newArrayList(versionInfo1, versionInfo2), ObjectType.PERSON, "TP1-TEST");
        final VersionLookupResult versionLookupResultMntner = new VersionLookupResult(Lists.newArrayList(versionInfo3), ObjectType.MNTNER, "TP1-TEST");
        when(versionDao.findByKey(ObjectType.PERSON, "TP1-TEST")).thenReturn(versionLookupResultPerson);
        when(versionDao.findByKey(ObjectType.MNTNER, "TP1-TEST")).thenReturn(versionLookupResultMntner);

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(new Query("--list-versions TP1-TEST", Query.Origin.LEGACY, false, queryMessages), responseHandler);

        final List<ResponseObject> responseObjects = responseHandler.getResponseObjects();

        assertThat(responseObjects, hasSize(4));
        assertThat(new String(responseObjects.get(0).toByteArray()),
                is(queryMessages.versionListStart(ObjectType.MNTNER.getName().toUpperCase(), "TP1-TEST").toString()));
    }

    @Test
    public void showVersion_person_role() {
        when(versionDao.getObjectType("TP1-TEST")).thenReturn(Collections.singleton(ObjectType.PERSON));
        setupVersionMock(versionInfo1, 1, 1312210585L);
        setupVersionMock(versionInfo2, 2, 1334066282L);

        final VersionLookupResult tp1 = new VersionLookupResult(Lists.newArrayList(versionInfo1, versionInfo2), ObjectType.PERSON, "TP1-TEST");
        when(versionDao.findByKey(ObjectType.PERSON, "TP1-TEST")).thenReturn(tp1);

        final RpslObject rpslObject = RpslObject.parse("" +
                "person: Tom Post\n" +
                "nic-hdl: TP1-TEST");
        when(versionDao.getRpslObject(any(VersionInfo.class))).thenReturn(rpslObject);

        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(new Query("--show-version 1 TP1-TEST", Query.Origin.LEGACY, false, queryMessages), responseHandler);

        final Iterator<ResponseObject> iterator = responseHandler.getResponseObjects().iterator();
        assertThat(iterator.hasNext(), is(true));
        assertThat(new String(iterator.next().toByteArray()), is(queryMessages.versionPersonRole("PERSON", "TP1-TEST").toString()));
    }

    private void setupVersionMock(VersionInfo mock, int objectId, long timestamp) {
        when(mock.getObjectId()).thenReturn(objectId);
        when(mock.getOperation()).thenReturn(Operation.UPDATE);
        when(mock.getTimestamp()).thenReturn(new VersionDateTime(new LocalDateTime(timestamp * 1000L, DateTimeZone.UTC)));
        when(mock.getSequenceId()).thenReturn(objectId - 1);
    }
}