class Project {
  Project({
    this.id,
    this.sender,
    this.time,
    this.subject,
    this.message,
    this.avatar,
    this.recipients,
    this.containsPictures,
  });

  int id;
  String sender;
  String time;
  String subject;
  String message;
  String avatar;
  String recipients;
  bool containsPictures;
}

class ProjectDetails extends Project {
  ProjectDetails({
    int id,
    String sender,
    String time,
    String subject,
    String message,
    String avatar,
    String recipients,
    bool containsPictures,
    this.inboxType = InboxType.normal,
  }) : super(
          id: id,
          sender: sender,
          time: time,
          subject: subject,
          message: message,
          avatar: avatar,
          recipients: recipients,
          containsPictures: containsPictures,
        );

  InboxType inboxType;
}

// The different mailbox pages that the Reply app contains.
enum MailboxPageType {
  inbox,
  starred,
  sent,
  trash,
  spam,
  drafts,
}

// Different types of mail that can be sent to the inbox.
enum InboxType {
  normal,
  spam,
}
