# URCap-with-USB-Camera-Vision

A URCap developed by the UR-Cobot Senior Design Team at Purdue University Indianapolis.

This URCap integrates a USB camera into PolyScope, providing a live video feed and object shape detection (circle, square, triangle, polygon) using OpenCV and XML-RPC.

---

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Repository Structure](#repository-structure)
- [Installation](#installation)
- [Running the Daemon](#running-the-daemon)
- [Building & Deploying the URCap](#building--deploying-the-urcap)
- [Usage](#usage)
- [Code Overview](#code-overview)
  - [Activator](#activator)
  - [Installation Node](#installation-node)
  - [Program Node](#program-node)
  - [XML-RPC Interface](#xml-rpc-interface)
  - [Python Daemon](#python-daemon)
- [License](#license)

---

## Features

- Live USB camera feed inside the PolyScope GUI
- Shape detection (circle, square, triangle, polygon)
- Manual shape-refresh via GUI button
- Automated camera preview refresh

---

## Prerequisites

- Universal Robots cobot with URCap SDK 1.13.0
- Java 8 or higher
- OpenCV for Python installed on the robot controller

---

## Repository Structure

```
URCap-with-USB-Camera-Vision/
├── src/               # Java URCap source code
│   ├── impl/
│   │   ├── Activator.java
│   │   ├── CamAppInstallationNodeContribution.java
│   │   ├── CamAppInstallationNodeView.java
│   │   ├── CamAppProgramNodeContribution.java
│   │   ├── CamAppProgramNodeView.java
│   │   ├── CamAppProgramNodeService.java
│   │   ├── CamAppInstallationNodeService.java
│   │   ├── CamDaemonService.java
│   │   ├── CamXmlRpcInterface.java
│   │   └── RpcResponseException.java
├── daemon/            # Python XML-RPC server
│   └── cam-daemon.py
├── build/             # URCap bundle output
└── README.md          # Project documentation
```

---

## Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/your-org/URCap-with-USB-Camera-Vision.git
   ```
2. Open the URCap project in your IDE (e.g., IntelliJ or Eclipse).
3. Build the URCap .jar (using Maven or Gradle as configured).
4. Copy the generated `.urcap` bundle into the robot controller's `/programs/URCaps` folder.

---

## Running the Daemon

On the robot controller shell:

```bash
cd /path/to/URCaps/URCap-with-USB-Camera-Vision/daemon
python cam-daemon.py
```

This starts an XML-RPC server on `127.0.0.1:40405` that:
- Captures camera frames (`captureImage()`)
- Returns the latest image path (`getImagePath()`)
- Detects shapes (`detectShapeType()`)
- Checks camera availability (`isCameraAvailable()`)

---

## Building & Deploying the URCap

In your IDE or via CLI:

```bash
# If using Maven
mvn clean package
# Copy URCap to robot
scp target/URCap-with-USB-Camera-Vision-1.0.urcap robot@<controller>:/programs/URCaps/
```  
Restart the URCap service on the robot to load the new version.

---

## Usage

1. In Polyscope, open the **Installation** tab and select **Vision Camera**.
2. Click **Start Server** to launch the XML-RPC daemon from Java.
3. Switch to the **Program** tab, add **Camera Feed** node.
4. The live camera preview will update automatically.
5. Press **Refresh** to run shape detection and update the "Detected shape:" label.
6. During script generation, the URCap will emit a call to the corresponding subroutine based on the last detected shape.

---

## Code Overview

### Activator

Registers the InstallationNodeService, ProgramNodeService, and DaemonService:
```java
@Override
public void start(BundleContext ctx) {
    CamDaemonService daemon = new CamDaemonService();
    ctx.registerService(SwingInstallationNodeService.class, new CamAppInstallationNodeService(daemon), null);
    ctx.registerService(SwingProgramNodeService.class, new CamAppProgramNodeService(), null);
    ctx.registerService(DaemonService.class, daemon, null);
}
```

### Installation Node

- **openView()**: starts monitoring daemon state and updates buttons & status every second
- **generateScript()**: writes a popup test for XML-RPC reachability

```java
Runnable uiUpdater = () -> view.setServerStatusLabel(getDaemonStateText());
executor.scheduleAtFixedRate(() -> EventQueue.invokeLater(uiUpdater), 0, 1, TimeUnit.SECONDS);
```

### Program Node

- **openView()**: starts daemon monitor + `startImageUpdateTimer()`
- **updateImage()**: snapshot & display preview every 250ms
- **onRefreshButtonClick()**: calls `updateImage()` + `updateShape()`
- **generateScript()**: emits URScript subroutine call based on `lastShape`

```java
public void generateScript(ScriptWriter w) {
  w.assign("shape", '"' + lastShape + '"');
  w.appendLine("if shape == \"square\": DrawSquare() ... end");
}
```

### XML-RPC Interface

Wraps Apache XmlRpcClient to call Python daemon endpoints:
```java
public String detectShapeType() throws Exception {
  Object res = client.execute("detectShapeType", new ArrayList<>());
  return parseString(res);
}
```

### Python Daemon

Listens on port 40405, using `SimpleXMLRPCServer`:
```python
server = SimpleXMLRPCServer(("127.0.0.1", 40405))
server.register_function(capture_image, "captureImage")
server.register_function(detect_shape_type, "detectShapeType")
server.serve_forever()
```

Shape detection uses OpenCV `findContours()` + `approxPolyDP()`:
```python
def detect_shape_type():
    contours, _ = cv2.findContours(...)
    cnt = max(contours, key=cv2.contourArea)
    approx = cv2.approxPolyDP(cnt, 0.02*peri, True)
    verts = len(approx)
    return {3: 'triangle', 4: 'square'}.get(verts, 'circle')
```

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

