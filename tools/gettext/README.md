This directory defines [gettext ITS rules][its] to teach xgettext how to extract messages from .gdxui files.

To make xgettext use these rules, the `$GETTEXTDATADIR` environment variable must point to this directory (`tools/gettext`), when calling it. The `po-update` Makefile target takes care of this.

[its]: https://www.gnu.org/software/gettext/manual/html_node/Preparing-ITS-Rules.html
