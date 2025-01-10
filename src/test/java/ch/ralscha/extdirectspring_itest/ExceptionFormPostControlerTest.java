/*
 * Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ralscha.extdirectspring_itest;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

@Disabled
public class ExceptionFormPostControlerTest extends JettyTest {

	private CloseableHttpClient client;

	private HttpPost post;

	@BeforeEach
	public void beforeTest() {
		this.client = HttpClientBuilder.create().build();
		this.post = new HttpPost("http://localhost:9998/controller/router");
	}

	@AfterEach
	public void afterTest() {
		try {
			if (this.client != null) {
				this.client.close();
			}
		}
		catch (final IOException ioe) {
			// ignore
		}
	}

	@Test
	public void testPost() throws IOException {

		List<NameValuePair> formparams = new ArrayList<>();
		formparams.add(new BasicNameValuePair("extTID", "3"));
		formparams.add(new BasicNameValuePair("extAction", "exceptionFormPostController"));
		formparams.add(new BasicNameValuePair("extMethod", "throwAException"));
		formparams.add(new BasicNameValuePair("extType", "rpc"));
		formparams.add(new BasicNameValuePair("extUpload", "false"));

		UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(formparams, "UTF-8");

		this.post.setEntity(postEntity);

		try (CloseableHttpResponse response = this.client.execute(this.post)) {
			HttpEntity entity = response.getEntity();
			assertThat(entity).isNotNull();
			String responseString = EntityUtils.toString(entity);
			ObjectMapper mapper = new ObjectMapper();

			Map<String, Object> rootAsMap = mapper.readValue(responseString, Map.class);
			assertThat(rootAsMap).hasSize(6);
			assertThat(rootAsMap.get("method")).isEqualTo("throwAException");
			assertThat(rootAsMap.get("type")).isEqualTo("exception");
			assertThat(rootAsMap.get("action")).isEqualTo("exceptionFormPostController");
			assertThat(rootAsMap.get("tid")).isEqualTo(3);
			assertThat(rootAsMap.get("message")).isEqualTo("a null pointer");
			assertThat(rootAsMap.get("where")).isNull();

			@SuppressWarnings("unchecked")
			Map<String, Object> result = (Map<String, Object>) rootAsMap.get("result");
			assertThat(result).hasSize(1);
			assertThat((Boolean) result.get("success")).isFalse();
		}
	}

}
