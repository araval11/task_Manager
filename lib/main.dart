import 'package:flutter/material.dart';
import 'package:task_manager/Authentication/login_page.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Login/Signup Demo',
      debugShowCheckedModeBanner: false,
      home: LoginPage(),
    );
  }
}
