IBIS-Lift Project

ABOUT ===========================================================
IBIS: Issue-based information systems
This project implements a Structured Conversation platform, similar to:
http://debategraph.org/
or
http://compendium.open.ac.uk/

Structured Conversations ========================================
Structured conversations consist of:
	Questions
	Answers
	Pro arguments
	Con arguments
	Reference nodes
Each entry, like a "tweet" or microblog entry, is a node. Nodes are
collected in tree structures.  In an indented outline form, a conversation looks like:

	Q: How do I participate in a structured conversation?
		A: Follow these steps
			A: 1- double-click a node to read in detail
			A: 2- select the Respond tab
			A: 3- select the type of response desired
				A: Question
				A: Answer
				A: Pro (argument)
				A: Con (argument)
				A: Reference (link to information)
			A: 4- Type in a response (very much like a tweet)
			A: 5- Optionally, add detailed descriptions 
			A: 6- Click the submit button

Structured conversations are a direct way to engage a group of participants
in researching a topic by creating and answering questions, justifying each answer,
and supporting or challenging claims.

Structured conversations require care to avoid confusion. A few important rules
of good conversation are these:

1- Limit each response to one topic.  By offering just one topic, it is then possible
to continue the conversation about that topic, and not waste words showing which of several
topics is in the conversation.

2- Try to make sure that another response does not say what you want to say (don't duplicate answers). 
Instead, if you find an answer that you like, and want to say more, add a response (Answer) to that node
that amplifies the earlier response.

3- It is frequently a good idea to ask a question rather than start an argument with a Con node.
Sometimes, a Con node, e.g. "That's impossible", does not convey any useful information, and risks
stopping an otherwise useful conversation branch.  A question, instead, can tease out of the idea
being challenged, some detail that might actually allow for discovery of unknown ideas.

A new conversation can start with an Idea (answer node), or a Question.

An idea is a well-posed statement that suggests a topic for conversation. Use the Details section to
explain the topic.  That topic might invite many different questions.

Starting a conversation with a well-posed question is a typical way to begin. When we say "well-posed",
we mean that the Details section is used to explain the nature of the question, background concepts, links
to further information, and so forth.

For more information, consider starting with slides found at
http://slideshare.net/jackpark/


Some websearch terms for structured conversations are:
	dialogue mapping
	issue mapping
	argument mapping


Road Ahead ======================================================
The IBIS-Lift project can stand alone as a powerful online platform for civic dialogue. We expect
to continue evolving this platform with more features, including tags and connections. For more
about connecting nodes, visit
http://cohere.open.ac.uk/

At the same time, this platform is a testbed for components that will serve a much larger web activity
we call knowledge gardening.

Getting Started =================================================

Configuring the platform:

The default.props file contains a number of important config values.
The most important one is
static.data.path  which must be set to the absolute path of the installed /static/ folder
inside webapps

Others are a bit more benign:

invite.required defaults to false; setting to true means that an Admin can add an
email address to an invitation list; only those on that invitation list can sign up.

authentication.required defaults to false; setting to true means that only those who
are logged in can see any content on the site.

landing.title defaults to "Conversations that matter"; it can be set to any message
desired to define the site.

From command line:

sbt

e.g.
c:\projects\IssueQuest\IBIS-Lift\IBISLift>sbt

c:\projects\IssueQuest\IBIS-Lift\IBISLift>set SCRIPT_DIR=c:\projects\IssueQuest\
IBIS-Lift\IBISLift\

c:\projects\IssueQuest\IBIS-Lift\IBISLift>java -XX:+CMSClassUnloadingEnabled -XX
:MaxPermSize=256m -Xmx512M -Xss2M -jar "c:\projects\IssueQuest\IBIS-Lift\IBISLif
t\\sbt-launcher.jar"
Getting Scala 2.7.7 ...

update

e.g.
> update
[info]
[info] == update ==
[info] :: retrieving :: Lift#lift-sbt-template_2.9.0-1 [sync]
[info]  confs: [compile, runtime, test, provided, system, optional, sources, jav
adoc]
[info]  28 artifacts copied, 0 already retrieved (21093kB/943ms)
[info] == update ==
[success] Successful.

jetty-run

e.g.
> jetty-run
[info]
[info] == copy-resources ==
[info] == copy-resources ==
[info]
[info] == compile ==
[info]   Source analysis: 30 new/modified, 0 indirectly invalidated, 0 removed.
[info] Compiling main sources...
etc

Then browse http://localhost:8080/
sign up (which logs you in).

Then click: "New Conversation"
Or click any conversation listed on the front page.