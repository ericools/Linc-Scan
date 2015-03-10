Presets
=======

This directory hosts various presets.

In production, this directory is supposed to be present on external storage,
which usually is external sd card.

It is provided here for reference and testing during development.

Following files are supposed to be present in this directory,
which will alter the behaviour of this application.

* department.csv (comma seperated values) for prefix/suffix feature.
   - 1st col should be numeric eg. 14
   - 2nd col will be parsed as prefix.
   - 3rd col will be parsed as suffix.
   - for example, 9,aa-,-bb
     will result in barcode result being prefixed with -aa and suffixed with -bb,
     if value in department field matches 1st col ie. 9
   - for no prefix use syntax 9,,-bb
   - for no suffix use syntax 9,aa-,
