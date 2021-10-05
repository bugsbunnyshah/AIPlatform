import 'dart:convert';

class CloudBusinessException implements Exception
{
  int statusCode;
  String message;

  CloudBusinessException(int statusCode, String message)
  {
    this.statusCode = statusCode;
    this.message = message;
  }

  String toString()
  {
    Map<String, dynamic> json = new Map();
    json['statusCode'] = statusCode;
    json['message'] = message;
    String jsonString = jsonEncode(json);
    return jsonString;
  }
}