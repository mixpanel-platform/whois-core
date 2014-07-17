package net.ripe.db.whois.common.query.executor.decorators;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.grs.AuthoritativeResource;
import net.ripe.db.whois.common.grs.AuthoritativeResourceData;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.common.query.domain.MessageObject;
import net.ripe.db.whois.common.query.QueryMessages;
import net.ripe.db.whois.common.query.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FilterPlaceholderDecoratorTest {

    @Mock
    SourceContext sourceContext;
    @Mock
    AuthoritativeResourceData authoritativeResourceData;
    @Mock
    AuthoritativeResource authoritativeResource;
    @Mock
    QueryMessages queryMessages;

    Source source;

    @InjectMocks
    FilterPlaceholdersDecorator subject;

    @Before
    public void setup() {
        source = Source.slave("TEST-GRS");
        when(sourceContext.getCurrentSource()).thenReturn(source);
        when(authoritativeResourceData.getAuthoritativeResource(any(CIString.class))).thenReturn(authoritativeResource);
        when(queryMessages.duplicateIpFlagsPassed()).thenReturn(new Message(Messages.Type.ERROR, ""));

        subject = new FilterPlaceholdersDecorator(sourceContext, authoritativeResourceData);
    }

    @Test
    public void filter_works() {
        when(sourceContext.isVirtual()).thenReturn(true);

        List<? extends ResponseObject> toFilter = Lists.newArrayList(
                RpslObject.parse("inetnum: 10.0.0.0 - 10.255.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255"));

        when(authoritativeResource.isMaintainedInRirSpace(any(RpslObject.class))).thenReturn(false, true, true);

        Iterator<? extends ResponseObject> result = subject.decorate(new Query("--resource 10.10.10.10", Query.Origin.LEGACY, false, queryMessages), toFilter).iterator();

        assertSame(result.next(), toFilter.get(1));
        assertSame(result.next(), toFilter.get(2));
        assertThat(result.hasNext(), is(false));
    }

    @Test
    public void messagesAreLeftAlone() {
        when(sourceContext.isVirtual()).thenReturn(true);
        List<? extends ResponseObject> toFilter = Lists.newArrayList(
                new MessageObject(queryMessages.duplicateIpFlagsPassed()),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.255.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.255.255"),
                RpslObject.parse("inetnum: 10.0.0.0 - 10.0.0.255"));

        when(authoritativeResource.isMaintainedInRirSpace(any(RpslObject.class))).thenReturn(false, false, true);

        Iterator<? extends ResponseObject> result = subject.decorate(new Query("--resource 10.10.10.10", Query.Origin.LEGACY, false, queryMessages), toFilter).iterator();

        assertSame(result.next(), toFilter.get(0));
        assertSame(result.next(), toFilter.get(3));
        assertThat(result.hasNext(), is(false));
    }

    @Test
    public void nonResourceQueriesAreFilteredAlone() {
        when(sourceContext.isVirtual()).thenReturn(true);
        List<? extends ResponseObject> toFilter = Collections.emptyList();

        Iterable<? extends ResponseObject> result = subject.decorate(new Query("10.10.10.10", Query.Origin.LEGACY, false, queryMessages), toFilter);

        assertNotSame(result, toFilter);
    }

    @Test
    public void nonVirtualSourcesAreLeftAlone() {
        when(sourceContext.isVirtual()).thenReturn(false);
        List<? extends ResponseObject> toFilter = Collections.emptyList();

        Iterable<? extends ResponseObject> result = subject.decorate(new Query("--resource 10.10.10.10", Query.Origin.LEGACY, false, queryMessages), toFilter);

        assertSame(result, toFilter);
    }
}
