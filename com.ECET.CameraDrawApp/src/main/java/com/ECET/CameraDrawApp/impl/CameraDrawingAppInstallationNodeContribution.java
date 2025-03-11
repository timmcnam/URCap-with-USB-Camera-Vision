package com.ECET.CameraDrawApp.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;

public class CameraDrawingAppInstallationNodeContribution implements InstallationNodeContribution {


    // Constants for DataModel keys
    private static final String DAEMON_RUNNING_KEY = "daemonRunning";
    private static final String AUTO_FOCUS_KEY = "autoFocus";
    private static final String FOCUS_LEVEL_KEY = "focusLevel";
    private static final String EXPOSURE_LEVEL_KEY = "exposureLevel";

    // Dependencies
    private final CameraDrawingAppInstallationNodeView view;
    private final DataModel model;
    private final CameraDaemonService daemonService;
    private final XmlRpcCameraDrawingAppDaemonInterface daemonInterface;

    // Executor for UI updates
    private ScheduledExecutorService executorService;

    public CameraDrawingAppInstallationNodeContribution(InstallationAPIProvider apiProvider,
            CameraDrawingAppInstallationNodeView view,
            DataModel model,
            CameraDaemonService daemonService,
            XmlRpcCameraDrawingAppDaemonInterface daemonInterface) {
    	this.view = view;
        this.model = model;
        this.daemonService = daemonService;
        this.daemonInterface = daemonInterface;
        this.executorService = Executors.newScheduledThreadPool(1);

        initializeDataModel();
        applyDaemonStatus();
	}

    private void initializeDataModel() {
        if (!model.isSet(DAEMON_RUNNING_KEY)) model.set(DAEMON_RUNNING_KEY, false);
        if (!model.isSet(AUTO_FOCUS_KEY)) model.set(AUTO_FOCUS_KEY, true);
        if (!model.isSet(FOCUS_LEVEL_KEY)) model.set(FOCUS_LEVEL_KEY, 50);
        if (!model.isSet(EXPOSURE_LEVEL_KEY)) model.set(EXPOSURE_LEVEL_KEY, 50);
    }

    @Override
    public void openView() {
        updateUI();
        restartExecutorService();
    }

    @Override
    public void closeView() {
        shutdownExecutorService();
    }

    @Override
    public void generateScript(ScriptWriter writer) {
        writer.assign("camera", "rpc_factory(\"xmlrpc\", \" http://127.0.0.1:40405/RPC2\")");
        
        writer.appendLine("if camera == None:");
        writer.appendLine("  popup(\"Error: Unable to establish XML-RPC connection.\", \"Error\", False, True, False)");
        writer.appendLine("  halt");
        writer.appendLine("if camera.ping():");
        writer.appendLine("  textmsg(\"Camera Daemon Connected\")");
        writer.appendLine("else:");
        writer.appendLine("  popup(\"Camera Daemon Unreachable\", \"Error\", False, True, False)");
    }

    public void onStartCameraPressed() {
        model.set(DAEMON_RUNNING_KEY, true);
        applyDaemonStatus();
        updateUI();
    }

    public void onStopCameraPressed() {
        model.set(DAEMON_RUNNING_KEY, false);
        applyDaemonStatus();
        updateUI();
    }

    public void toggleAutoFocus() {
        boolean autoFocus = model.get(AUTO_FOCUS_KEY, true);
        model.set(AUTO_FOCUS_KEY, !autoFocus);
        updateUI();

        executeAsync(() -> {
            try {
                daemonInterface.setAutoFocus(!autoFocus);
            } catch (Exception e) {
                logError("Failed to toggle auto-focus", e);
            }
        });
    }

    public void adjustFocus(int amount) {
        int newFocus = clampValue(model.get(FOCUS_LEVEL_KEY, 50) + amount, 0, 100);
        model.set(FOCUS_LEVEL_KEY, newFocus);
        updateUI();

        executeAsync(() -> {
            try {
                daemonInterface.setFocusLevel(newFocus);
            } catch (Exception e) {
                logError("Failed to adjust focus", e);
            }
        });
    }

    public void adjustExposure(int amount) {
        int newExposure = clampValue(model.get(EXPOSURE_LEVEL_KEY, 50) + amount, 0, 100);
        model.set(EXPOSURE_LEVEL_KEY, newExposure);
        updateUI();

        executeAsync(() -> {
            try {
                daemonInterface.setExposureLevel(newExposure);
            } catch (Exception e) {
                logError("Failed to adjust exposure", e);
            }
        });
    }

    private void applyDaemonStatus() {
        executeAsync(() -> {
            boolean shouldRun = model.get(DAEMON_RUNNING_KEY, false);
            try {
                if (shouldRun) {
                    if (daemonInterface.ping()) {
                        daemonService.getDaemon().start();
                    } else {
                        logError("Daemon is unreachable, not starting.", null);
                    }
                } else {
                    daemonService.getDaemon().stop();
                }
            } catch (Exception e) {
                logError("Failed to manage daemon status", e);
            }
        });
    }

    private void updateUI() {
        boolean isRunning = model.get(DAEMON_RUNNING_KEY, false);
        view.updateCameraStatus(isRunning ? "Camera Running" : "Camera Offline");
        view.setStartButtonEnabled(!isRunning);
        view.setStopButtonEnabled(isRunning);
    }

    private void restartExecutorService() {
        if (executorService.isShutdown() || executorService.isTerminated()) {
            executorService = Executors.newScheduledThreadPool(1);
        }
        executorService.scheduleAtFixedRate(() -> SwingUtilities.invokeLater(this::updateUI), 0, 2, TimeUnit.SECONDS);
    }

    private void shutdownExecutorService() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void executeAsync(Runnable task) {
        new Thread(task).start();
    }

    private void logError(String message, Exception e) {
        System.err.println(message + (e != null ? ": " + e.getMessage() : ""));
    }

    private int clampValue(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

}
