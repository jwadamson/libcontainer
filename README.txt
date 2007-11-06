[Usage]
  (In properties dialog for your project)
    - select 'Java Build Path'
    - select 'Libraries' tab
    - click  'Add Library' button 
  (In Add Library dialog)
    - choose 'Library Container'
    - choose the folder



[For developers of LibClasspathContainer plugin]
  1) Prepare
    * Import this project to your Eclipse (without LibClasspathContainer plugin itself).
    * connect to the SVN repository
      (right click on the project and choose 'Team' ...)
    * execute SVN update
      - The newest source is located on the SVN repository
      - (Subversive plugin is recommended. Subclipse plugin may be available...)
  2) Rebuild
    * set your minor version:
      - open build.xml on this directory
      - edit the value of "version" property
    * execute "build-all" ant task
  3) Install your minor version
    * copy the directory 'buildRelease/site' to arbitrary location outside this project [[@1]]
    * Select as follows
      - Help -> Software Updates -> Find and Install
      - Search for new features to install
      - New Local Site
      - choose the [[@1]] location (copied 'site' directory)
  4) Please upload your modifications to the Tracker
     (https://sourceforge.net/tracker/?group_id=205995)
      - the modified source code(.java)
      - .patch file
        (if you cannot, the output of 'diff' command)
