/**
 * @author <a href="mailto:lgao@redhat.com">Lin Gao </a>
 */
package org.jboss.eap.trackers.data.db;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.ws.rs.core.MediaType;

/**
 * 
 * Pretty Print Servlet Filter for JSON output.
 * 
 * @author lgao
 *
 */
@WebFilter(filterName = "jsonPPFilter", urlPatterns = {"/api"})
public class JSONPrettyPrintFilter implements Filter
{

   private final static String INITIAL_PRETTY_NAME = "pretty.print.parameter.name";
   
   private final static String DEFAULT_PRETTY_PARAM_NAME = "pretty";
   
   private String prettyName;
   
   @Override
   public void init(FilterConfig filterConfig) throws ServletException
   {
      prettyName = filterConfig.getInitParameter(INITIAL_PRETTY_NAME);
      if (prettyName == null || prettyName.length() == 0) {
         prettyName = DEFAULT_PRETTY_PARAM_NAME;
      }
   }

   @Override
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
   {
      // Pretty print JSON out.
      String contentType = response.getContentType();
      
      // only filter JSON format
      if (MediaType.APPLICATION_JSON.equals(contentType)) {
         // whether required by end user.
         boolean pretty = false;
         if (request.getParameter(prettyName) != null || request.getAttribute(prettyName) != null) {
            pretty = true;
         }
         if (pretty) {
            PrettyPrintResponse resp = new PrettyPrintResponse((HttpServletResponse)response);
            resp.setHeader("Transfer-Encoding", "chunked"); // content length will be changed.
            chain.doFilter(request, resp);
            return;
         }
      }
      chain.doFilter(request, response);
   }

   @Override
   public void destroy()
   {
      ;
   }
   
   private class PrettyPrintResponse extends HttpServletResponseWrapper {

      private HttpServletResponse response;
      
      private PPOutputStream output = new PPOutputStream();
      
      public PrettyPrintResponse(HttpServletResponse response)
      {
         super(response);
         this.response = response;
      }
      
      private class PPOutputStream extends ServletOutputStream
      {

         @Override
         public void write(int b) throws IOException
         {
           response.getOutputStream().write(b);
         }
         
         @Override
         public void close() throws IOException
         {
            response.getOutputStream().close();
         }
         
         @Override
         public void flush() throws IOException
         {
            response.getOutputStream().flush();
         }
         
      }
      
      @Override
      public void setContentLength(int len)
      {
         // do nothing, should be chucked content encoding 
      }
      
      
      @Override
      public ServletOutputStream getOutputStream() throws IOException
      {
         return output;
      }
      
   }

}
