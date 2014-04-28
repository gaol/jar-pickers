/**
 * 
 */
package org.jboss.eap.trackers.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.HashMap;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

import org.jboss.security.ClientLoginModule;
import org.jboss.security.SecurityContextAssociation;

/**
 * @author lgao
 *
 */
public class SecurityContext {

	private static LoginContext lc;
	
	static {
		SecurityContextAssociation.setClient();
	}
	
	/**
	 * this loginmodule does not care sharedstate or options
	 * 
	 * It does simple basic login process.
	 *
	 */
	
	private static class TrakcerCallBackHandler implements CallbackHandler {

		@Override
		public void handle(Callback[] callbacks) throws IOException,
				UnsupportedCallbackException {
			for (Callback cb: callbacks) {
				if (cb instanceof NameCallback) {
					NameCallback nameCB = (NameCallback)cb;
					System.err.print("Please input the UserName: ");
					
					String name = new BufferedReader(new InputStreamReader(System.in)).readLine();
					if (name != null && name.trim().length() > 0) {
						nameCB.setName(name);
					}
				}
				else if (cb instanceof PasswordCallback) {
					PasswordCallback passCB = (PasswordCallback)cb;
					System.err.print("Password: ");
					passCB.setPassword(readPassword(System.in));
				}
				else {
					throw new UnsupportedCallbackException(cb, "Unkown Callback: " + cb.getClass().getName());
				}
			}
		}
		
		private char[] readPassword(InputStream in) throws IOException {

			char[] lineBuffer;
			char[] buf;
			buf = lineBuffer = new char[128];
			
			int room = buf.length;
			int offset = 0;
			int c;

			loop: while (true) {
				switch (c = in.read()) {
				case -1:
				case '\n':
					break loop;

				case '\r':
					int c2 = in.read();
					if ((c2 != '\n') && (c2 != -1)) {
						if (!(in instanceof PushbackInputStream)) {
							in = new PushbackInputStream(in);
						}
						((PushbackInputStream) in).unread(c2);
					} else
						break loop;

				default:
					if (--room < 0) {
						buf = new char[offset + 128];
						room = buf.length - offset - 1;
						System.arraycopy(lineBuffer, 0, buf, 0, offset);
						Arrays.fill(lineBuffer, ' ');
						lineBuffer = buf;
					}
					buf[offset++] = (char) c;
					break;
				}
			}

			if (offset == 0) {
				return null;
			}

			char[] ret = new char[offset];
			System.arraycopy(buf, 0, ret, 0, offset);
			Arrays.fill(buf, ' ');
			return ret;
		}
		
	}

	private static LoginContext getLoginContext() throws LoginException {
		if (lc == null) {
			Configuration config = new Configuration() {
				
				@Override
				public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
					AppConfigurationEntry entry = new AppConfigurationEntry(ClientLoginModule.class.getName(),
							LoginModuleControlFlag.REQUIRED, new HashMap<String, Object>());
					return new AppConfigurationEntry[]{entry};
				}
			};
			lc = new LoginContext("DUMMY", new Subject(), new TrakcerCallBackHandler(), config);
		}
		return lc;
	}
	
	public static void login() throws LoginException {
		if (!isLogged()) {
			LoginContext lc = getLoginContext();
			lc.login();
		}
		else {
			System.out.println("Has logged in already!");
		}
	}
	
	public static void logout() throws LoginException {
		if (isLogged()) {
			LoginContext lc = getLoginContext();
			lc.logout();
		}
		else {
			System.out.println("Not logged in yet!");
		}
	}
	
	public static boolean isLogged() {
		return SecurityContextAssociation.getPrincipal() != null;
	}
}
