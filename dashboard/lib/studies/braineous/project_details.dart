import 'package:flutter/material.dart';
import 'package:gallery/layout/adaptive.dart';
import 'package:gallery/studies/braineous/model/project_model.dart';
import 'package:provider/provider.dart';

import 'project_card_preview.dart';
import 'model/project_store.dart';

class ProjectDetails extends StatelessWidget {
  const ProjectDetails({Key key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    print("*******MAIL_BOX_INVOKED*******");
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
        List<Project> emails;

        switch (destination) {
          case MailboxPageType.inbox:
            {
              emails = model.inboxEmails;
              break;
            }
          case MailboxPageType.sent:
            {
              emails = model.outboxEmails;
              break;
            }
          case MailboxPageType.starred:
            {
              emails = model.starredEmails;
              break;
            }
          case MailboxPageType.trash:
            {
              emails = model.trashEmails;
              break;
            }
          case MailboxPageType.spam:
            {
              emails = model.spamEmails;
              break;
            }
          case MailboxPageType.drafts:
            {
              emails = model.draftEmails;
              break;
            }
        }

        return SafeArea(
          bottom: false,
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: emails.isEmpty
                    ? Center(child: Text('Empty in $destinationString'))
                    : ListView.separated(
                        itemCount: 3,
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
                          var email = emails[index];
                          return ProjectPreviewCard(
                            id: email.id,
                            email: email,
                            isStarred: model.isEmailStarred(email.id),
                            onDelete: () => model.deleteEmail(email.id),
                            onStar: () => model.starEmail(email.id),
                            onStarredMailbox: model.selectedMailboxPage ==
                                MailboxPageType.starred,
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
