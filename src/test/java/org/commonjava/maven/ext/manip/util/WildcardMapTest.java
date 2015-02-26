package org.commonjava.maven.ext.manip.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import junit.framework.TestCase;
import org.commonjava.maven.atlas.ident.ref.ProjectRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

public class WildcardMapTest extends TestCase
{
    private WildcardMap map;
    private ListAppender<ILoggingEvent> m_listAppender;

    @Before
    public void setUp() throws Exception
    {
        m_listAppender = new ListAppender<ILoggingEvent>();
        m_listAppender.start();

        Logger root = (Logger) LoggerFactory.getLogger(WildcardMap.class);
        root.addAppender(m_listAppender);

        map = new WildcardMap();
    }

    @After
    public void tearDown() throws Exception
    {
        map = null;

        Logger root = (Logger) LoggerFactory.getLogger(WildcardMap.class);
        root.detachAppender(m_listAppender);
    }

    @Test
    public void testContainsKey() throws Exception
    {
        map.put(ProjectRef.parse("org.group:new-artifact"), "1.2");

        assertFalse(map.containsKey(ProjectRef.parse("org.group:*")));
        assertFalse(map.containsKey(ProjectRef.parse("org.group:old-artifact")));
    }

    @Test
    public void testGet() throws Exception
    {
        final String value = "1.2";
        ProjectRef key1 = ProjectRef.parse("org.group:new-artifact");
        ProjectRef key2 = ProjectRef.parse("org.group:new-new-artifact");

        map.put(key1, value);
        map.put(key2, value);

        assertTrue(value.equals(map.get(key1)));
        assertTrue(value.equals(map.get(key2)));
    }

    @Test
    public void testGetSingle() throws Exception
    {
        final String value = "1.2";

        map.put(ProjectRef.parse("org.group:new-artifact"), value);

        assertFalse(value.equals(map.get(ProjectRef.parse("org.group:i-dont-exist-artifact"))));
    }

    @Test
    public void testPut() throws Exception
    {
        ProjectRef key = ProjectRef.parse("foo:bar");

        map.put(key, "value");
        assertTrue("Should have retrieved value", map.containsKey(key));
    }

    @Test
    public void testPutWildcard() throws Exception
    {
        ProjectRef key1 = ProjectRef.parse("org.group:*");
        ProjectRef key2 = ProjectRef.parse("org.group:artifact");
        ProjectRef key3 = ProjectRef.parse("org.group:new-artifact");

        map.put(key1, "1.1");

        assertTrue("Should have retrieved wildcard value", map.containsKey(key2));
        assertTrue("Should have retrieved wildcard value", map.containsKey(key1));

        map.put(key3, "1.2");

        assertTrue("Should have retrieved wildcard value", map.containsKey(key2));
        assertTrue("Should have retrieved wildcard value", map.containsKey(key1));

        assertThat(m_listAppender.list.toString(),
                containsString("Unable to add org.group:new-artifact with value 1.2 as wildcard mapping for org.group already exists"));

    }

    @Test
    public void testPutWildcardSecond() throws Exception
    {
        ProjectRef key1 = ProjectRef.parse("org.group:artifact");
        ProjectRef key2 = ProjectRef.parse("org.group:*");

        map.put(key1, "1.1");
        map.put(key2, "1.2");

        assertTrue("Should have retrieved explicit value via wildcard", map.containsKey(key1));
        assertTrue("Should have retrieved wildcard value", map.containsKey(key2));
        assertFalse("Should not have retrieved value 1.1", map.get(key1).equals("1.1"));

        assertThat(m_listAppender.list.toString(),
                containsString("Emptying map with keys [artifact] as replacing with wildcard mapping org.group:*"));

    }
}