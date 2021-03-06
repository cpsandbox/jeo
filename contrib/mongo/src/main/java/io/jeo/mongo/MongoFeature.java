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
package io.jeo.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vividsolutions.jts.geom.Geometry;
import io.jeo.vector.Feature;
import io.jeo.vector.MapFeature;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Map;

/**
 * Feature wrapper around a mongo {@link DBObject}.
 * <p>
 * This class uses a {@link Mapping} instance to map feature geometries and properties.
 * </p>
 * @author Justin Deoliveira, OpenGeo
 */
public class MongoFeature implements Feature {

    String id;
    DBObject obj;
    Mapping mapping;

    public MongoFeature(DBObject obj, Mapping mapping) {
        this.id = id(obj);
        this.obj = obj;
        this.mapping = mapping;
    }

    public DBObject object() {
        return obj;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean has(String key) {
        boolean has = mapping.getGeometryPath(key) != null;
        if (!has) {
            Object f = find(mapping.getPropertyPath());
            if (f instanceof DBObject) {
                has = ((DBObject) f).containsField(key);
            }
        }
        return has;
    }

    @Override
    public Object get(String key) {
        Path g = mapping.getGeometryPath(key);
        if (g != null) {
            return GeoJSON.toGeometry((DBObject)find(g));
        }
        return find(mapping.getPropertyPath().append(key));
    }

    @Override
    public Geometry geometry() {
        if (!mapping.getGeometryPaths().isEmpty()) {
            return GeoJSON.toGeometry((DBObject)find(mapping.getGeometryPaths().get(0)));
        }
        return null;
    }

    @Override
    public Feature put(Geometry g) {
        if (!mapping.getGeometryPaths().isEmpty()) {
            return put(mapping.getGeometryPaths().iterator().next().join(), g);
        }

        throw new IllegalArgumentException("No geometry for feature");
    }

    @Override
    public Feature put(String key, Object val) {
        Object dbval = val;
        if (val instanceof Geometry) {
            dbval = GeoJSON.toObject((Geometry) val);
        }

        for (Path g : mapping.getGeometryPaths()) {
            if (g.join().equals(key)) {
                if (val instanceof Geometry){
                    set(g, dbval);
                    return this;
                }
                else {
                    throw new IllegalArgumentException("Value " + val + " is not a geometry");
                }
            }
        }

        return set(mapping.getPropertyPath().append(key), dbval);
    }

    @Override
    public Map<String, Object> map() {
        Map<String, Object> map = obj.toMap();

        for (Path g : mapping.getGeometryPaths()) {
            DBObject geo = (DBObject) find(g);
            map.put(g.join(), GeoJSON.toGeometry(geo));
        }

        return map;
    }

    protected Object find(Path path) {
        DBObject obj = this.obj;
        Object next = obj;

        List<String> parts = path.getParts();
        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            next = obj.get(part);
            if (next == null) {
                return null;
            }

            if (i < parts.size()-1) {
                if (!(next instanceof DBObject)) {
                    throw new IllegalArgumentException("Illegal path, " + part + " is not an object");
                }
                obj = (DBObject) next;
            }
        }
        return next;
    }

    protected Feature set(Path path, Object val) {
        DBObject obj = this.obj;
        List<String> parts = path.getParts();
        for (int i = 0; i < parts.size()-1; i++) {
            String part = parts.get(i);
            Object next = obj.get(part);
            if (next == null) {
                next = new BasicDBObject();
                obj.put(part, next);
            }

            if (!(next instanceof DBObject)) {
                throw new IllegalArgumentException("Illegal path, " + part + " is not an object");
            }
            obj = (DBObject)next;
        }

        obj.put(parts.get(parts.size()-1), val);
        return this;
    }

    static String id(DBObject obj) {
        ObjectId id = (ObjectId) obj.get("_id");
        id = id != null ? id : ObjectId.get();
        return id.toString();
    }
}
