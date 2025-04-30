package com.ECET.CamApp.impl;

import java.awt.EventQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.ur.urcap.api.contribution.DaemonContribution;
import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.installation.CreationContext;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

public class CamAppInstallationNodeContribution implements InstallationNodeContribution{
	
	private static final String CAMERA_KEY ="camera";
	private static final String SERVER_KEY = "server";
	private static final String XMLRPC_VARIABLE = "cam_app"; // Name of the URScript variable for XML-RPC connection
	private static final String ENABLED_KEY = "enabled";
	private static final String CHECK_KEY = "check";
	private static final String CAMERA_DEFAULT_VALUE = "Camera has not been turned on.";
	private static final String SERVER_DEFAULT_VALUE = "Server has not been started.";
	private static final long DAEMON_TIME_OUT_NANO_SECONDS = TimeUnit.SECONDS.toNanos(20);
	private static final long RETRY_TIME_TO_WAIT_MILLI_SECONDS = TimeUnit.SECONDS.toMillis(1);
	
	private CamAppInstallationNodeView view; 			  // GUI
	private CamDaemonService daemonService;				  // Daemon Service class
	private final CamXmlRpcInterface daemonStatusMonitor; // Helper class to monitor daemon state and make XML-RPC calls
	private final DataModel model; 						  // Stores persistent data like status messages
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> scheduleAtFixedRate;
	
	public CamAppInstallationNodeContribution(InstallationAPIProvider apiProvider, 
											  CamAppInstallationNodeView view, 
											  CamDaemonService daemonService, 
											  CamXmlRpcInterface xmlRpcDaemonInterface,
											  CreationContext context,
											  DataModel model) {
		
		this.view = view;
		this.model = model;
		this.daemonService = daemonService;
		this.daemonStatusMonitor = xmlRpcDaemonInterface; // connects the backend to the XML-RPC daemon
		if (context.getNodeCreationType() == CreationContext.NodeCreationType.NEW) {
			model.set(CAMERA_KEY, CAMERA_DEFAULT_VALUE);
			model.set(SERVER_KEY, SERVER_DEFAULT_VALUE);
		}
		applyDesiredDaemonStatus(); // Starts or stops the daemon based on the saved state
	}

	@Override
	public void openView() {
		/* Verify the server has been started and that the default text
		* has been updated to show that the server is started.
		* 
		*/ 
		
		view.setCameraStatusLabel(getCameraText());
		view.setServerStatusLabel(getServerText());
		daemonStatusMonitor.startMonitorThread(); // Checks daemon status every second
		
		//UI updates from non-GUI threads must use EventQueue.invokeLater (or SwingUtilities.invokeLater)
		Runnable updateUIRunnable = new Runnable() {
			
			@Override
			public void run() {
				EventQueue.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						updateUI();
					}
				});
			}
		};
		if (scheduleAtFixedRate != null) {
			scheduleAtFixedRate.cancel(true);
		}
		scheduleAtFixedRate = executorService.scheduleAtFixedRate(updateUIRunnable, 0, 1, TimeUnit.SECONDS);
		
	}

	@Override
	public void closeView() {
		/* Stop updating the UI and cancel the monitor thread
		 * Stops everything gracefully when the tab is closed.
		 * 
		 */
		if (scheduleAtFixedRate != null) {
			scheduleAtFixedRate.cancel(true);
		}
		daemonStatusMonitor.stopMonitorThread();
		
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// Assign XML-RPC variable
		writer.assign(XMLRPC_VARIABLE, "rpc_factory(\"xmlrpc\", \"" + CamXmlRpcInterface.getDaemonUrl() + "\")");
		
		// Call a test method to check connection and show popup
		writer.assign("daemon_ok", XMLRPC_VARIABLE + ".isReachable()"); 
		writer.appendLine("if daemon_ok:");
		writer.appendLine("  popup(\"Camera server is reachable\", \"XML-RPC\", blocking=True)");
		writer.appendLine("else:");
		writer.appendLine("  popup(\"Camera server is NOT reachable\", \"XML-RPC\", blocking=True)");
		writer.appendLine("end");
	}
	
	private void updateUI() {
		
		// also based on the state of the daemon, we need to set the titles accordingly
		DaemonContribution.State state = getDaemonState();
		
		String text = "";
		switch (state) {
			case RUNNING:
				view.setStartButtonEnabled(false);
				view.setStopButtonEnabled(true);
				view.setCheckCamButtonEnabled(true);
				text = "Camera Daemon runs";
				break;
			case STOPPED:
				view.setStartButtonEnabled(true);
				view.setStopButtonEnabled(false);
				view.setCheckCamButtonEnabled(false);
				text = "Camera Daemon stopped";
				break;
			case ERROR:
			default:
				view.setStartButtonEnabled(true);
				view.setStopButtonEnabled(false);
				view.setCheckCamButtonEnabled(false);
				text = "Camera Daemon failed";
				break;
		}

		view.setServerStatusLabel(text);
		
	}
	
	
	private Boolean isDaemonEnabled() {
		return model.get(ENABLED_KEY, true);
	}
	
	public String getXMLRPCVariable() {
		return XMLRPC_VARIABLE;
	}
	
	public String getServerText() {
		return model.get(SERVER_KEY, SERVER_DEFAULT_VALUE);
	}
	
	public String getCameraText() {
		return model.get(CAMERA_KEY, CAMERA_DEFAULT_VALUE);
	}
	
	private DaemonContribution.State getDaemonState() {
		return daemonStatusMonitor.isDaemonReachable() ? daemonService.getDaemon().getState() : DaemonContribution.State.STOPPED;
	}
	
	public void onStartCLick() {
		model.set(ENABLED_KEY, true);
		applyDesiredDaemonStatus();
		
	}
	
	// Stop button press
	public void onStopCLick() {
		model.set(ENABLED_KEY, false);
		applyDesiredDaemonStatus();
	}
	
	// Check Camera Button press
	public void onCheckClick() {
		model.set(CHECK_KEY, true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result;
				if (daemonStatusMonitor.isDaemonReachable()) {
					try {
						boolean available = daemonStatusMonitor.isCameraAvailable();
						result = available ? "Camera is available" : "Camera not found";
					} catch (Exception e) {
						result = "Camera check error";
						e.printStackTrace();
					}
				} else {
					result = "Daemon not reachable";
				}

				final String cameraStatus = result;

				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						view.setCameraStatusLabel(cameraStatus);
						model.set(CHECK_KEY, false);
					}
				});
			}
		}).start();
		
	}
	
	// Starts or stops the daemon based on whether it's enabled (from the model).
	public void applyDesiredDaemonStatus() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				if (CamAppInstallationNodeContribution.this.isDaemonEnabled()) {
					try {
						CamAppInstallationNodeContribution.this.awaitDaemonRunning();
						model.set(SERVER_KEY, "Server has been started.");
					} catch (Exception e) {
						System.err.println("Could not start the server.");
						Thread.currentThread().interrupt();
					}
				}
				else {
					daemonService.getDaemon().stop();
				}
			}
		}).start();
	}
	
	public void awaitDaemonRunning() throws InterruptedException {
		daemonService.getDaemon().start();
		long endTime = System.nanoTime() + DAEMON_TIME_OUT_NANO_SECONDS;
		while(System.nanoTime() < endTime) {
			if (daemonStatusMonitor.isDaemonReachable()) {
				break;
			}
			Thread.sleep(RETRY_TIME_TO_WAIT_MILLI_SECONDS);
		}
	}
	
	public CamXmlRpcInterface getDaemonStatusMonitor() {
		return daemonStatusMonitor;
	}

}
