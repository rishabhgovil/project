package com.surajsararf.musicoplayer;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

import static org.junit.Assert.*;

/**
 * Created by RISHABH on 27-11-2017.
 */
public class FullTestSuite extends TestSuite{

    public static Test suite(){
        return new TestSuiteBuilder(FullTestSuite.class)
                .includeAllPackagesUnderHere().build();
    }
    public FullTestSuite(){
        super();
    }

}