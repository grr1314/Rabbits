package com.kyleduo.rabbits;

import android.net.Uri;
import android.os.Bundle;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Find TargetInfo matching the url.
 * <p>
 * Created by kyle on 30/01/2018.
 */

public class ActionParser implements Interceptor {
    @Override
    public DispatchResult intercept(Dispatcher dispatcher) {
        Action action = dispatcher.action();
        Uri uri = action.getUri();

        // generate uri
        if (uri == null) {
            String url = action.getOriginUrl();
//            "" ~ "/"
//            if (TextUtils.isEmpty(url)) {
//                return DispatchResult.error("Url can not be empty");
//            }
            if (url.contains("://")) {
                uri = Uri.parse(url);
            } else {
                if (!url.startsWith("/")) {
                    url = "/" + url;
                }
                url = Rabbit.get().getSchemes().get(0) + "://" + Rabbit.get().getDomains().get(0) + url;
                uri = Uri.parse(url);
            }
            action.setUri(uri);
        }

        TargetInfo target = RouteTable.match(action.getUri());

        action.setTargetType(target == null ? TargetInfo.TYPE_NONE : target.type);
        action.setTargetFlags(target == null ? 0 : target.flags);
        action.setTargetPattern(target == null ? null : target.pattern);
        action.setTargetClass(target == null ? null : target.target);

        if (target != null) {
            // params from REST url
            Bundle bundle = new Bundle();
            Map<String, Object> urlParams = target.params;
            if (urlParams != null) {
                for (Map.Entry<String, Object> entry : urlParams.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof Integer) {
                        bundle.putInt(key, (Integer) value);
                    } else if (value instanceof Float) {
                        bundle.putFloat(key, (Float) value);
                    } else if (value instanceof Double) {
                        bundle.putDouble(key, (Double) value);
                    } else if (value instanceof String) {
                        bundle.putString(key, (String) value);
                    } else {
                        bundle.putString(key, value.toString());
                    }
                }
            }

            Set<String> keys;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                keys = uri.getQueryParameterNames();
            } else {
                keys = getQueryParameterNames(uri);
            }
            if (keys != null && keys.size() > 0) {
                for (String key : keys) {
                    String params = uri.getQueryParameter(key);
                    bundle.putString(key, params);
                }
            }

            if (action.getExtras() != null) {
                bundle.putAll(action.getExtras());
            }
            action.setExtras(bundle);
        }

        return dispatcher.dispatch(action);
    }


    /**
     * Fetch keys from an Uri object. The code is from SDK 25.
     *
     * @param uri the Uri
     * @return A set of keys.
     */
    private static Set<String> getQueryParameterNames(Uri uri) {
        if (uri.isOpaque()) {
            throw new UnsupportedOperationException("This isn't a hierarchical URI.");
        }

        String query = uri.getEncodedQuery();
        if (query == null) {
            return Collections.emptySet();
        }

        Set<String> names = new LinkedHashSet<>();
        int start = 0;
        do {
            int next = query.indexOf('&', start);
            int end = (next == -1) ? query.length() : next;

            int separator = query.indexOf('=', start);
            if (separator > end || separator == -1) {
                separator = end;
            }

            String name = query.substring(start, separator);
            names.add(decode(name));

            // Move start to end of name.
            start = end + 1;
        } while (start < query.length());

        return Collections.unmodifiableSet(names);
    }

    private static String decode(String origin) {
        String out;
        try {
            out = URLDecoder.decode(origin, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            out = origin;
        }
        return out;
    }
}
