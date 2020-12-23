/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.upstream;

import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.android.exoplayer2.testutil.DataSourceContractTest;
import com.google.android.exoplayer2.testutil.TestUtil;
import com.google.android.exoplayer2.testutil.WebServerDispatcher;
import com.google.common.collect.ImmutableList;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

/** {@link DataSource} contract tests for {@link DefaultHttpDataSource}. */
@RunWith(AndroidJUnit4.class)
public class DefaultHttpDataSourceContractTest extends DataSourceContractTest {

  private static int seed = 0;
  private static final WebServerDispatcher.Resource RANGE_SUPPORTED =
      new WebServerDispatcher.Resource.Builder()
          .setPath("/supports/range-requests")
          .setData(TestUtil.buildTestData(/* length= */ 20, seed++))
          .supportsRangeRequests(true)
          .build();

  private static final WebServerDispatcher.Resource RANGE_SUPPORTED_LENGTH_UNKNOWN =
      new WebServerDispatcher.Resource.Builder()
          .setPath("/supports/range-requests-length-unknown")
          .setData(TestUtil.buildTestData(/* length= */ 20, seed++))
          .supportsRangeRequests(true)
          .resolvesToUnknownLength(true)
          .build();

  private static final WebServerDispatcher.Resource RANGE_NOT_SUPPORTED =
      new WebServerDispatcher.Resource.Builder()
          .setPath("/doesnt/support/range-requests")
          .setData(TestUtil.buildTestData(/* length= */ 20, seed++))
          .supportsRangeRequests(false)
          .build();

  private static final WebServerDispatcher.Resource RANGE_NOT_SUPPORTED_LENGTH_UNKNOWN =
      new WebServerDispatcher.Resource.Builder()
          .setPath("/doesnt/support/range-requests-length-unknown")
          .setData(TestUtil.buildTestData(/* length= */ 20, seed++))
          .supportsRangeRequests(false)
          .resolvesToUnknownLength(true)
          .build();

  private final MockWebServer mockWebServer = new MockWebServer();

  @Before
  public void startServer() throws Exception {
    mockWebServer.start();
    mockWebServer.setDispatcher(
        WebServerDispatcher.forResources(
            ImmutableList.of(
                RANGE_SUPPORTED,
                RANGE_SUPPORTED_LENGTH_UNKNOWN,
                RANGE_NOT_SUPPORTED,
                RANGE_NOT_SUPPORTED_LENGTH_UNKNOWN)));
  }

  @After
  public void shutdownServer() throws Exception {
    mockWebServer.shutdown();
  }

  @Override
  protected DataSource createDataSource() {
    return new DefaultHttpDataSource.Factory().createDataSource();
  }

  @Override
  protected ImmutableList<TestResource> getTestResources() {
    return ImmutableList.of(
        toTestResource("range supported", RANGE_SUPPORTED),
        toTestResource("range supported, length unknown", RANGE_SUPPORTED_LENGTH_UNKNOWN),
        toTestResource("range not supported", RANGE_NOT_SUPPORTED),
        toTestResource("range not supported, length unknown", RANGE_NOT_SUPPORTED_LENGTH_UNKNOWN));
  }

  @Override
  protected Uri getNotFoundUri() {
    return Uri.parse(mockWebServer.url("/not/a/real/path").toString());
  }

  private TestResource toTestResource(String name, WebServerDispatcher.Resource resource) {
    return new TestResource.Builder()
        .setName(name)
        .setUri(Uri.parse(mockWebServer.url(resource.getPath()).toString()))
        .setExpectedBytes(resource.getData())
        .setResolvesToUnknownLength(resource.resolvesToUnknownLength())
        .setEndOfInputExpected(!resource.resolvesToUnknownLength())
        .build();
  }
}