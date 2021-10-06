import 'package:flutter/material.dart';
import 'package:gallery/layout/adaptive.dart';
import 'package:gallery/studies/braineous/model/project_store.dart';
import 'package:provider/provider.dart';

import 'project_card_preview.dart';
import 'model/project_model.dart';

class ProjectBody extends StatelessWidget {
  Future<List<Project>> future;

  ProjectBody(){
    ProjectStore store = new ProjectStore();
    this.future = store.getProjects();
  }

  @override
  Widget build(BuildContext context) {
    FutureBuilder<List<Project>> builder = FutureBuilder(
      future: this.future,
      builder: (context, AsyncSnapshot<List<Project>> snapshot) {
        if (snapshot.hasData) {
          return consumer(context,snapshot.data);
        } else {
          return CircularProgressIndicator();
        }
      }
    );
    return builder;
  }

  Consumer<ProjectStore> consumer(BuildContext context, List<Project> projects){
    final isDesktop = isDisplayDesktop(context);
    final isTablet = isDisplaySmallDesktop(context);
    final startPadding = isTablet
        ? 60.0
        : isDesktop
        ? 120.0
        : 4.0;
    final endPadding = isTablet
        ? 30.0
        : isDesktop
        ? 60.0
        : 4.0;
    return Consumer<ProjectStore>(
      builder: (context, model, child) {
        final destination = model.selectedMailboxPage;
        final destinationString = destination
            .toString()
            .substring(destination.toString().indexOf('.') + 1);
        return SafeArea(
          bottom: false,
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: projects.isEmpty
                    ? Center(child: Text('Empty in $destinationString'))
                    : ListView.separated(
                  itemCount: projects.length,
                  padding: EdgeInsetsDirectional.only(
                    start: startPadding,
                    end: endPadding,
                    top: isDesktop ? 28 : 0,
                    bottom: kToolbarHeight,
                  ),
                  primary: false,
                  separatorBuilder: (context, index) =>
                  const SizedBox(height: 4),
                  itemBuilder: (context, index) {
                    print("**********EMAIL_ITEM*****");
                    var project = projects[index];
                    return ProjectPreviewCard(
                      project:project,
                    );
                  },
                ),
              ),
              if (isDesktop) ...[
                Padding(
                  padding: const EdgeInsetsDirectional.only(top: 14),
                  child: Row(
                    children: [
                      IconButton(
                        key: const ValueKey('ReplySearch'),
                        icon: const Icon(Icons.search),
                        onPressed: () {
                          Provider.of<ProjectStore>(
                            context,
                            listen: false,
                          ).onSearchPage = true;
                        },
                      ),
                      SizedBox(width: isTablet ? 30 : 60),
                    ],
                  ),
                ),
              ]
            ],
          ),
        );
      },
    );
  }
}
