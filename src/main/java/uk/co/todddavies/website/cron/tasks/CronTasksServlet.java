package uk.co.todddavies.website.cron.tasks; 

import uk.co.todddavies.website.cron.tasks.Annotations.TaskId;
import uk.co.todddavies.website.cron.tasks.data.RecurringTask;
import uk.co.todddavies.website.cron.tasks.data.TaskDatastoreInterface;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
@SuppressWarnings("serial")
final class CronTasksServlet extends HttpServlet {
  
  private static final Logger log = Logger.getLogger(CronTasksServlet.class.getName());
  private static final String HABITICA_ENDPOINT = "https://habitica.com/api/v3/tasks/user";
  private static final String ERROR_MESSAGE = 
      "Habitica API returned an invalid response:\nCode: %d\nMessage:%s";
  private static final ImmutableSet<Integer> SUCCESS_CODES = ImmutableSet.of(200, 201);
  
  private final Provider<Optional<Long>> taskIdProvider;
  private final TaskDatastoreInterface taskStorage;
  
  @Inject
  private CronTasksServlet(@TaskId Provider<Optional<Long>> taskIdProvider,
      TaskDatastoreInterface taskStorage) {
    this.taskIdProvider = taskIdProvider;
    this.taskStorage = taskStorage;
  }
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String cronHeader = req.getHeader("X-Appengine-Cron");
    if(cronHeader == null || !cronHeader.equals("true")) {
      log.warning("Not called from Cron.");
      resp.setStatus(401);
      return;
    }
    Optional<Long> taskId = taskIdProvider.get();
    if (!taskId.isPresent()) {
      log.warning("Task ID not supplied to endpoint");
      resp.setStatus(200);
      return;
    }
    // Fetch task from datastore
    Optional<RecurringTask> task = taskStorage.get(taskId.get());
    if (!task.isPresent()) {
      log.warning(String.format("Task with ID %s not found!", taskId.get()));
      resp.setStatus(200);
      return;
    }
    // Execute http request to Habitica
    try {
      executeTask(task.get());
      resp.setStatus(200);
    } catch (RuntimeException e) {
      String errorString = "API request to Habitica failed.\nTask: %s";
      log.log(Level.WARNING, String.format(errorString, task.get()), e);
      resp.sendError(500, "Api request to Habitica failed");
    }
  }
  
  
  private static final void executeTask(RecurringTask task)
      throws ClientProtocolException, IOException {
    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost(HABITICA_ENDPOINT);
    // Stop the client from moaning about cookies
    client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
    
    post.setHeader("x-api-user", HabiticaApiKeys.USER);
    post.setHeader("x-api-key", HabiticaApiKeys.KEY);
    post.setHeader("Content-Type", "application/x-www-form-urlencoded");
    
    List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
    urlParameters.add(new BasicNameValuePair("type", "todo"));
    urlParameters.add(new BasicNameValuePair("notes", task.getNotes()));
    urlParameters.add(new BasicNameValuePair("text", task.getName()));
    post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));

    HttpResponse response = client.execute(post);
    int statusCode = response.getStatusLine().getStatusCode();
    if (!SUCCESS_CODES.contains(statusCode)) {
      String message = extractMessage(response);
      throw new RuntimeException(
          String.format(ERROR_MESSAGE, statusCode, message));
    }
  }
  
  private static String extractMessage(HttpResponse response)
      throws IllegalStateException, IOException {
    BufferedReader rd = 
        new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

    StringBuffer result = new StringBuffer();
    String line = "";
    while ((line = rd.readLine()) != null) {
      result.append(line);
    }
    return result.toString();
  }
}
