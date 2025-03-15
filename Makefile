INPUT = target/
NAME = "Autoclicker"
VERSION = 1.0.0
DESCRIPTION = "AutoClicker made with Java that supports multiple clicking locations"
build:
	mvn clean package


install: build
	jpackage --verbose --name ${NAME} \
		--input $(INPUT) \
		--main-jar AutoClicker-1.0.jar \
		--main-class AutoClicker \
		--name $(NAME) \
		--app-version $(VERSION) \
		--vendor "Ellefsen" \
		--description $(DESCRIPTION) \
		--type dmg \
		--icon mouse.ico \
		--mac-entitlements mac/entitlements.plist \
		--mac-package-identifier dev.ellefsen \
		--mac-package-name EllefsenAutoClicker \


