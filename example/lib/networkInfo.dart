import 'dart:async';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:meta/meta.dart';

class NetworkConnection extends ChangeNotifier {
  bool _hasInternetConnection = false;
  bool get hasInternetConnection => _hasInternetConnection;

  Future<bool> get isConnected => _checkInternetAccess();

  @override
  void dispose() {
    super.dispose();
  }

  /// If any of the pings returns true then you have internet (for sure). If none do, you probably don't.
  Future<bool> _checkInternetAccess() {
    /// We use a mix of IPV4 and IPV6 here in case some networks only accept one of the types.
    /// Only tested with an IPV4 only network so far (I don't have access to an IPV6 network).
    final List<InternetAddress> dnss = [
      InternetAddress('192.168.1.1', type: InternetAddressType.IPv4), // Google

      InternetAddress('8.8.8.8', type: InternetAddressType.IPv4), // Google
    ];

    final Completer<bool> completer = Completer<bool>();

    int callsReturned = 0;
    void onCallReturned(bool isAlive) {
      if (completer.isCompleted) return;

      if (isAlive) {
        completer.complete(true);
      } else {
        callsReturned++;
        if (callsReturned >= dnss.length) {
          completer.complete(false);
        }
      }
    }

    dnss.forEach((dns) => _pingDns(dns).then(onCallReturned));

    return completer.future;
  }

  Future<bool> _pingDns(InternetAddress dnsAddress) async {
    const int dnsPort = 53;
    const Duration timeout = Duration(seconds: 2);

    Socket? socket;
    try {
      socket = await Socket.connect(dnsAddress, dnsPort, timeout: timeout);
      socket.destroy();
      setConnectionStatus(isConnected: true);
      return true;
    } on SocketException {
      socket?.destroy();
    }
    setConnectionStatus(isConnected: false);
    return false;
  }

  void setConnectionStatus({required bool isConnected}) {
    _hasInternetConnection = isConnected;
    notifyListeners();
  }
}
