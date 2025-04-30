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

- Universal Robots **eSeries** cobot with with Polyscoper version 5.11 or higher.
- Java 8 or higher.
- **OpenCV** for Python 2.7 installed on the robot controller.
- **numbpy** for Python 2.7 installed on the robot controller.

Note: To use this URCap, **the libraries must be installed**. SSH into the controller and install [pip](https://pip.pypa.io/en/stable/installation/). Once this is done, you can then execute pip install opencv-python and numbpy. You will get the version needed for what version of python is running in the Linux Image on the controller.

If you do not want to do this, you can have a seperate resouce folder for necessary libraries. However, you will need to set up your own development environment and create you own urcap. 

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

```bash
git clone https://github.com/timmcnam/URCap-with-USB-Camera-Vision.git
```
1. Open in your IDE (IntelliJ/Eclipse).  
2. Build with Maven/Gradle → produces `.urcap`.  
3. Copy to robot:  
   ```bash
   scp target/*.urcap robot@<controller>:/programs/URCaps/
   ```

---

## Running the Daemon

```bash
cd daemon
python cam-daemon.py
```
The daemon listens on `127.0.0.1:40405` and exposes:
- `captureImage()`  
- `getImagePath()`  
- `detectShapeType()`  
- `isCameraAvailable()`  

---

## Building & Deploying the URCap

```bash
# Build
mvn clean package

# Deploy
scp target/URCap-with-USB-Camera-Vision-*.urcap robot@<controller>:/programs/URCaps/
```
Restart URCaps on the robot to load.

---

## Usage

1. In Polyscope → **Installation** tab → select **Vision Camera**.  
2. Click **Start Server**.  
3. Switch to **Program** tab → add **Camera Feed** node.  
4. Live preview auto-updates.  
5. Press **Refresh** to classify shape and update label.  
6. Generated script calls the subroutine matching last detected shape.

---

## Code Overview

### Activator

Registers services for installation, program node, and daemon:

```java
public void start(BundleContext ctx) {
  CamDaemonService daemon = new CamDaemonService();
  ctx.registerService(InstallationNodeService.class, new CamAppInstallationNodeService(daemon), null);
  ctx.registerService(ProgramNodeService.class, new CamAppProgramNodeService(), null);
  ctx.registerService(DaemonService.class, daemon, null);
}
```

### Installation Node

- **openView()**: starts a 1 Hz UI‐update thread to toggle Start/Stop buttons.  
- **generateScript()**: injects a simple XML-RPC reachability popup.

### Program Node

- **openView()** calls `startImageUpdateTimer()` (250 ms interval).  
- **updateImage()** grabs a frame and `view.displayImage(...)`.  
- **onRefreshButtonClick()** calls:
  ```java
  updateImage();
  updateShape();
  ```
- **updateShape()** invokes `detectShapeType()` and `view.setShapeText(...)`.  
- **generateScript()** emits URScript:
  ```python
  if shape=="square": DrawSquare()
  elif shape=="circle": DrawCircle()
  …
  end
  ```

### XML-RPC Interface

Wraps Apache XmlRpcClient to talk with Python daemon:

```java
public String detectShapeType() throws XmlRpcException { 
  Object res = client.execute("detectShapeType", new ArrayList<>()); 
  return parseString(res); 
}
```

### Python Daemon

Key sections from `daemon/cam-daemon.py`:

```python
from SimpleXMLRPCServer import SimpleXMLRPCServer
from SocketServer import ThreadingMixIn

class MultithreadedXMLRPCServer(ThreadingMixIn, SimpleXMLRPCServer): pass

server = MultithreadedXMLRPCServer(("127.0.0.1",40405))
server.register_function(capture_image,      "captureImage")
server.register_function(get_image_path,     "getImagePath")
server.register_function(detect_shape_type,  "detectShapeType")
server.register_function(is_camera_available,"isCameraAvailable")
server.serve_forever()
```

**Shape detection** uses OpenCV:

```python
def detect_shape_type():
    # binarize, findContours, pick largest
    cnt = max(contours, key=cv2.contourArea)
    approx = cv2.approxPolyDP(cnt, 0.02*cv2.arcLength(cnt,True), True)
    verts = len(approx)
    return {3:"triangle",4:"square"}.get(verts, "polygon")
```

---

## License

This project is MIT-licensed. See [LICENSE](LICENSE).  
```
