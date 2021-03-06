package org.wso2.siddhi.extension.wordcloud;

/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.extension.wordcloud.test.util.SiddhiTestHelper;
import org.wso2.siddhi.core.util.EventPrinter;

public class WordcloudSlidingwindowExtensionTestCase {
    static final Logger log = Logger.getLogger(WordcloudSlidingwindowExtensionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testContainsFunctionExtension() throws InterruptedException {
        log.info("WordcloudSlidingwindowExtensionTestCase SlidingWindow TestCase ");
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "@config(async = 'true')define stream inputStream (text string,Rt int ,Ft int);";
        String query = ("@info(name = 'query1') " + "from inputStream#Cloud:getCloudG1P1(1,50,text) "
                + "select TopWords as processedText,1 as id " + "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager
                .createExecutionPlanRuntime(inStreamDefinition + query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        Assert.assertEquals("Value: FAIR", inEvent.getData(0).toString().split(",")[0]);
                    }
                    if (count.get() == 2) {

                        Assert.assertEquals("Value: A", inEvent.getData(0).toString().split(",")[0]);
                    }
                    if (count.get() == 3) {

                        Assert.assertEquals("Value: A", inEvent.getData(0).toString().split(",")[0]);
                    }
                    if (count.get() == 4) {

                        Assert.assertEquals("Value: OF", inEvent.getData(0).toString().split(",")[0]);
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler
                .send(new Object[] {
                        "Very fair complaint about Computer Science research, http://neverworkintheory.org/2016/04/26/perception-productivity.html#research",
                        10, 5 });
        inputHandler
                .send(new Object[] {
                        "We're having a Q&A - one of UoJ the students shares the trial and error process of an Asana project they did a while ago.",
                        1, 50 });
        inputHandler
                .send(new Object[] {
                        "A connector allows the WSO2 platform to connect to other services, like Twitter. That's part of what our Platform Extension team works on.",
                        10, 90 });
        inputHandler
                .send(new Object[] {
                        "Malaka Silva of @wso2 talking about @Google's Summer of Code to folks from the University of Jaffna, especially on getting in.",
                        94, 75 });
        SiddhiTestHelper.waitForEvents(100, 4, count, 120000);
        Assert.assertEquals(4, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }
}
