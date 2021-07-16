DESKTOP_JAR=$(CURDIR)/desktop/build/libs/desktop-1.0.jar
TOOLS_JAR=$(CURDIR)/tools/build/libs/tools-1.0.jar
GRADLEW=./gradlew
ifdef OFFLINE
	GRADLEW=./gradlew --offline
endif

GAME_CP=com.agateau.pixelwheels
EXECUTABLE=pixelwheels

include version.properties

DIST_OUT_BASE_DIR=dist-out
DIST_NAME=$(EXECUTABLE)-$(VERSION)
DIST_OUT_DIR=$(DIST_OUT_BASE_DIR)/$(DIST_NAME)

DESKTOP_RUN_DIST_NAME=$(EXECUTABLE)-$(VERSION)-linux64
ANDROID_ITCHIO_RUN_DIST_NAME=$(EXECUTABLE)-itchio-$(VERSION)
ANDROID_GPLAY_RUN_DIST_NAME=$(EXECUTABLE)-gplay-$(VERSION)

ARCHIVE_DIR=$(CURDIR)/archives

ANDROID_PACKAGE_NAME=com.agateau.tinywheels.android

ifdef SNAPSHOT
	VERSION:=$(VERSION)+$(shell git show --no-patch --format="%cd-%h" --date=format:%Y%d%mT%H%M%S)
endif

all: build

clean: clean-assets
	rm -f $(DESKTOP_JAR) $(TOOLS_JAR)

$(TOOLS_JAR):
	${GRADLEW} tools:dist

build:
	${GRADLEW} desktop:dist

tools: $(TOOLS_JAR)

run: build
	cd android/assets && java -jar $(DESKTOP_JAR)

packer: tools assets
	java -cp $(TOOLS_JAR) $(GAME_CP).tools.Packer

mapscreenshotgenerator: tools
	java -cp $(TOOLS_JAR) $(GAME_CP).tools.MapScreenshotGenerator

assets:
	$(MAKE) -C core/assets-src

clean-assets:
	$(MAKE) -C core/assets-src clean

# Automatically regenerates assets when ase files are modified (requires the
# `entr` command)
auto-assets:
	find core/assets-src -name '*.ase' | entr $(MAKE) assets packer

# Dist
desktop-dist: build
	@rm -rf $(DIST_OUT_BASE_DIR)
	@mkdir -p $(DIST_OUT_BASE_DIR)

	@echo Creating desktop archives
	@tools/create-archives $(VERSION)

	@echo Moving desktop archives
	@mkdir -p $(ARCHIVE_DIR)
	mv -v $(DIST_OUT_BASE_DIR)/*.zip $(ARCHIVE_DIR)

apk-dist:
	@echo Creating apk files
	@$(GRADLEW) android:assembleRelease
	@echo Moving apk files
	@mkdir -p $(ARCHIVE_DIR)
	@for store in itchio gplay ; do \
		mv android/build/outputs/apk/$$store/release/android-$$store-release.apk $(ARCHIVE_DIR)/$(EXECUTABLE)-$$store-$(VERSION).apk ; \
	done


dist: assets packer check desktop-dist apk-dist

clean-dist: clean dist

desktop-run-from-dist:
	@echo "This target only works on Linux right now"
	rm -rf tmp
	mkdir -p tmp
	unzip $(ARCHIVE_DIR)/$(DESKTOP_RUN_DIST_NAME).zip -d tmp
	tmp/$(DESKTOP_RUN_DIST_NAME)/pixelwheels

android-run-from-dist:
	# uninstall any existing verison in case we have an unsigned version installed
	adb uninstall $(ANDROID_PACKAGE_NAME) || true
	adb install -f $(ARCHIVE_DIR)/$(ANDROID_ITCHIO_RUN_DIST_NAME).apk
	adb shell am start -n $(ANDROID_PACKAGE_NAME)/com.agateau.pixelwheels.android.AndroidLauncher

# coding style
codingstyle-check:
	tools/apply-codingstyle --check

codingstyle-apply:
	tools/apply-codingstyle

# Tag
tag:
	git tag -f -m "Pixel Wheels $(VERSION)" $(VERSION)

tagpush: tag
	git push
	git push --tags

# Uploading
fastlane-beta:
	fastlane supply --track beta --apk $(ARCHIVE_DIR)/$(ANDROID_GPLAY_RUN_DIST_NAME).apk

upload:
	ci/upload-build pixelwheels $(ARCHIVE_DIR)/$(DIST_NAME)-*.*

# Cleaning conf
clean-desktop-conf:
	rm -f ~/.config/agateau.com/pixelwheels.conf
	rm -rf ~/.local/share/pixelwheels

clean-android-conf:
	adb shell "pm clear $(ANDROID_PACKAGE_NAME)"

check: codingstyle-check
	@$(GRADLEW) check
	@$(GRADLEW) test

# Translations
po-update:
	@echo "Listing files to translate"
	@rm -f po/files
	@for dir in core desktop android ; do \
		find $$dir -name '*.java' | grep -v '/build/' >> po/files ; \
	done

	@echo "Extracting messages"
	@xgettext --from-code=utf-8 --keyword=tr --keyword=trn:1,2 --output po/messages.pot --files-from po/files

	@for po_file in po/*.po ; do \
		echo "Updating $$po_file" ; \
		msgmerge --update $$po_file po/messages.pot ; \
	done

.PHONY: desktop-dist apk-dist dist clean-dist tag tagpush fastlane-beta check tools build release-archives
