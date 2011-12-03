package org.apache.karaf.cellar.core;


import java.io.InputStream;
import java.util.Properties;
import org.apache.karaf.cellar.core.event.EventType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;


import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class CellarSupportTest {

    ConfigurationAdmin configurationAdmin = createMock(ConfigurationAdmin.class);
    Configuration configuration = createMock(Configuration.class);
    Properties props = new Properties();

    Group defaultGroup = new Group("default");

    @Before
    public void setUp() throws Exception {
        InputStream is = getClass().getResourceAsStream("groups.properties");
        props.load(is);
        is.close();
        expect(configurationAdmin.getConfiguration(EasyMock.<String>anyObject())).andReturn(configuration).anyTimes();
        expect(configuration.getProperties()).andReturn(props).anyTimes();
        replay(configuration);
        replay(configurationAdmin);
    }

    @After
    public void tearDown() throws Exception {
        verify(configuration);
        verify(configurationAdmin);
    }

    @Test
    public void testIsAllowed() {
        CellarSupport support = new CellarSupport();
        support.setConfigurationAdmin(configurationAdmin);

        Boolean expectedResult = false;
        Boolean result = support.isAllowed(defaultGroup,"config","org.apache.karaf.shell", EventType.INBOUND);
        assertEquals("Shell should not be allowed",expectedResult,result);

        expectedResult = true;
        result = support.isAllowed(defaultGroup,"config","org.apache.karaf.myshell", EventType.INBOUND);
        assertEquals("MyShell should be allowed",expectedResult,result);
    }
}