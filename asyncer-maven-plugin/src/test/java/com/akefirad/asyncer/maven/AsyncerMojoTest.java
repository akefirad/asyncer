package com.akefirad.asyncer.maven;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;

public class AsyncerMojoTest extends AbstractMojoTestCase {

    public void testJustMessage() throws Exception {
        File pom = PlexusTestCase.getTestFile("src/test/resources/pom.xml");
        assertNotNull(pom);
        assertTrue(pom.exists());
        AsyncerMojo myMojo = (AsyncerMojo) lookupMojo("asyncer", pom);
        assertNotNull(myMojo);
        myMojo.execute();
    }

}
