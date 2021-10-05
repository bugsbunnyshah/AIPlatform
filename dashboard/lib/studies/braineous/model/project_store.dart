import 'dart:convert';
import 'package:flutter/widgets.dart';
import 'package:gallery/studies/braineous/model/urlFunctions.dart';

import 'cloudBusinessException.dart';
import 'project_model.dart';

import 'package:http/http.dart' as http;

const _avatarsLocation = 'reply/avatars';


class ProjectStore with ChangeNotifier {
  List<Project> get spamEmails {
    return null;
  }

  List<Project> get starredEmails {
    return null;
  }

  int _selectedEmailId = -1;
  int get selectedEmailId => _selectedEmailId;
  set selectedEmailId(int value) {
    _selectedEmailId = value;
    notifyListeners();
  }

  bool get onMailView => _selectedEmailId > -1;

  MailboxPageType _selectedMailboxPage = MailboxPageType.inbox;
  MailboxPageType get selectedMailboxPage => _selectedMailboxPage;
  set selectedMailboxPage(MailboxPageType mailboxPage) {
    _selectedMailboxPage = mailboxPage;
    notifyListeners();
  }

  bool _onSearchPage = false;
  bool get onSearchPage => _onSearchPage;
  set onSearchPage(bool value) {
    _onSearchPage = value;
    notifyListeners();
  }

  Future<List<Project>> getProjects() async
  {
    List<Project> projects = [];

    var response;
    Map<String, dynamic> json;

    String remoteUrl = "http://localhost/projects/";
    try {
      response = await http.get(Uri.parse(remoteUrl)).
      timeout(Duration(seconds: 30),onTimeout: () {
        print("NETWORK_TIMEOUT");
        //json = new Map();
        //json["exception"] = "NETWORK_TIME_OUT";
        //json["statusCode"] = 500;
        throw new CloudBusinessException(500, "NETWORK_TIME_OUT");
      });
    }
    catch (e) {
      print(e);
      json = UrlFunctions.handleError(e, response);
      return [];
    }


    Map<String,dynamic> errorJson = UrlFunctions.handleError(null, response);
    if(errorJson != null)
    {
      return [];
    }

    json  = jsonDecode(response.body) as Map<String, dynamic>;

    String projectId = json['projects'].first['projectId'].toString();
    ProjectDetails local = ProjectDetails(
      id: 1,
      sender: projectId,
      time: '3 Artifacts',
      subject: 'Aviation AI Model',
      message: 'Status: Development -> QA',
      avatar: '$_avatarsLocation/avatar_express.png',
      recipients: '',
      containsPictures: false,
    );
    projects.add(local);

    return projects;
  }
}
