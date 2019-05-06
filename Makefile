
clean:
	./gradlew clean

plugin:
	./gradlew :gradle-junit-reports:uploadArchives -c settings-local.gradle

run: plugin
	./gradlew commitCheck vcsCheck ciBuild

test: clean plugin
	./gradlew :gradle-junit-reports:test
