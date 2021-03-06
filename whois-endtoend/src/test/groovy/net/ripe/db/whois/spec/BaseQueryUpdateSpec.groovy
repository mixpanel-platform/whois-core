package net.ripe.db.whois.spec
import com.google.common.collect.Maps
import com.google.common.collect.Sets
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper
import net.ripe.db.whois.common.rpsl.RpslObject

abstract class BaseQueryUpdateSpec extends BaseEndToEndSpec {
    def setupSpec() {
        DatabaseHelper.setupDatabase()
        whoisFixture.start()
    }

    def setup() {
        databaseHelper.clearAclLimits()
        databaseHelper.insertAclIpLimit("0/0", -1, true)
        databaseHelper.insertAclIpLimit("::0/0", -1, true)

        allFixtures();
    }

    def oneBasicFixture(String key) {
        def s = BasicFixtures.basicFixtures[key]
        if (s == null) {
            throw new IllegalArgumentException('No fixture for ${key}')
        }

        return object(s)
    }

    Map<String, String> getFixtures() {
        Maps.newHashMap()
    }

    Map<String, String> getBasicFixtures() {
        BasicFixtures.basicFixtures
    }

    private void allFixtures() {
        def rpslObjects = Sets.newHashSet();
        rpslObjects.addAll(basicFixtures.values().collect { RpslObject.parse(it.stripIndent()) })
        rpslObjects.addAll(fixtures.values().collect { RpslObject.parse(it.stripIndent()) })

        getDatabaseHelper().addObjects(rpslObjects)
    }

    String getFixture(String key) {
        def s = fixtures[key]
        if (s == null) {
            throw new IllegalArgumentException('No fixture for ${key}')
        }

        return s.stripIndent()
    }

    Map<String, String> getTransients() {
        Maps.newHashMap()
    }

    String getTransient(String key) {
        def s = transients[key]
        if (s == null) {
            throw new IllegalArgumentException('No transient for ${key}')
        }

        return s.stripIndent()
    }

    def dbfixture(String string) {
        getDatabaseHelper().addObject(string)
        string
    }

    def addTag(String pkey, String tag, String data) {
        getDatabaseHelper().getWhoisTemplate().update("INSERT INTO tags(object_id, tag_id, data) SELECT object_id, \"${tag}\", \"${data}\" from last where pkey='${pkey}'");
    }

    def grepQueryLog(String pattern) {
        boolean result = false;
        getTestWhoisLog().messages.each { line ->
            if (line =~ pattern) result = true;
        }
        result
    }
}
