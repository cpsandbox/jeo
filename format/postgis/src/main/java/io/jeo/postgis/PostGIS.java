/* Copyright 2013 The jeo project. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jeo.postgis;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.jeo.vector.VectorDriver;
import io.jeo.vector.Schema;
import io.jeo.util.Key;
import io.jeo.util.Messages;
import io.jeo.util.Password;

import static io.jeo.util.Util.set;

public class PostGIS implements VectorDriver<PostGISWorkspace>{

    /**
     * Database key.
     */
    public static final Key<String> DB = new Key<>("db", String.class).alias("database");

    /**
     * Schema key.
     */
    public static final Key<String> SCHEMA = new Key<String>("schema", String.class);

    /**
     * Host key, defaults to 'localhost'.
     */
    public static final Key<String> HOST = new Key<String>("host", String.class, "localhost");

    /**
     * Port key, defaults to 5432.
     */
    public static final Key<Integer> PORT = new Key<Integer>("port", Integer.class, 5432);

    /**
     * User key, defaults to current user, obtained via <tt>System.getProperty("user.name")</tt>
     */
    public static final Key<String> USER = 
            new Key<String>("user", String.class, System.getProperty("user.name"));

    /**
     * Password key. 
     */
    public static final Key<Password> PASSWD = new Key<Password>("passwd", Password.class);

    public static PostGISWorkspace open(PostGISOpts opts) throws IOException {
        return new PostGISWorkspace(opts);
    }

    @Override
    public boolean isEnabled(Messages messages) {
        return true;
    }

    @Override
    public String name() {
        return "PostGIS";
    }

    @Override
    public List<String> aliases() {
        return Arrays.asList("pg", "pgsql");
    }

    @Override
    public Class<PostGISWorkspace> type() {
        return PostGISWorkspace.class;
    }

    @Override
    public List<Key<? extends Object>> keys() {
        return (List) Arrays.asList(DB, SCHEMA, HOST, PORT, USER, PASSWD);
    }

    @Override
    public String family() {
        return "jeo";
    }

    @Override
    public boolean canOpen(Map<?, Object> opts, Messages msgs) {
        if (!DB.in(opts)) {
            Messages.of(msgs).report("No " + DB + " option specified");
            return false;
        }

        return true;
    }

    @Override
    public PostGISWorkspace open(Map<?, Object> opts) throws IOException {
        return new PostGISWorkspace(PostGISOpts.fromMap(opts));
    }

    @Override
    public boolean canCreate(Map<?, Object> opts, Messages msgs) {
        Messages.of(msgs).report("Creation not supported");
        return false;
    }

    @Override
    public PostGISWorkspace create(Map<?, Object> opts, Schema schema) throws IOException {
        throw new UnsupportedOperationException();
    }

    static final Set<Capability> CAPABILITIES =
        set(CREATE, DESTROY, APPEND, UPDATE, BOUND, LIMIT, OFFSET, FILTER, FIELD);

    @Override
    public Set<Capability> capabilities() {
        return CAPABILITIES;
    }
}
