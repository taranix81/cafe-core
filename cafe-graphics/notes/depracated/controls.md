## Notes

* each Widget required Shell control to be created and cannot be null
* only Shell component can listen to Close cafeOldEvent
* creating Control required in many cases run
  ``shell.layout()``
* GridLayout required GridData on child control
* Listeners need to be applied after creation of Control
* cannot use Dispose Event to inform child controls that application is going to be closed
* Composite must have Layout setup

## Conceptual questions

* If Shell has set a GridLayout how and what GridData should be passed to child Control ?
* What main elements Application should be defined and where should be placed?
    * Menu Bar
    * Tool bar
    * Status bar
    * Container for Document, Controls etc?
* what default Layout should be used?
* How to create, configure and manage content of Application (Container)

