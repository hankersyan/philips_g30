package io.hankers.mdi.philips_g30;

import io.hankers.mdi.mdi_utils.MDIConfig;
import io.hankers.mdi.mdi_utils.MDILog;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
		int port = MDIConfig.getListenPort();
		try {
			new DataListener(port).start();
		} catch (Exception e) {
			MDILog.e(e);
		}
    }
}
