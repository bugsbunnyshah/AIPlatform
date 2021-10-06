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

  ProjectPages _selectedMailboxPage = ProjectPages.inbox;
  ProjectPages get selectedMailboxPage => _selectedMailboxPage;
  set selectedMailboxPage(ProjectPages mailboxPage) {
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
      response = await http.get(Uri.parse(remoteUrl),headers: {
        "Principal":"-2061008798",
        "Bearer": "blah",
      },).
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
    Iterable l = json['projects'] as Iterable;
    for (Map<String, dynamic> project in l) {
      String projectId = project['projectId'].toString();
      Project local = Project(
        projectId: projectId,
        projectName: "Aviation Data Model",
      );
      projects.add(local);
    }

    return projects;
  }
}

// The different mailbox pages that the Reply app contains.
enum ProjectPages {
  inbox,
  starred,
  sent,
  trash,
  spam,
  drafts,
}
