package com.kyleduo.rabbits;

import android.util.SparseArray;

/**
 * Final phase of navigation.
 *
 * Created by kyle on 26/01/2018.
 */

class NavigatorInterceptor implements InternalInterceptor {

    private SparseArray<Navigator> mNavigators;

    NavigatorInterceptor(SparseArray<Navigator> navigators) {
        mNavigators = navigators;
    }

    @Override
    public RabbitResult intercept(Dispatcher dispatcher) {
        Logger.i("[!] Navigating...");
        if (mNavigators == null) {
            throw new NullPointerException("No verify navigator");
        }
        Action action = dispatcher.action();

        // Just return the target when "justObtain" was specific.
        if (action.isJustObtain() && action.getTarget() != null) {
            return RabbitResult.success(action.getTarget());
        }

        boolean notFound = false;
        if (action.getTarget() == null || action.getTargetType() == TargetInfo.TYPE_NONE) {
            notFound = true;
            if (action.isIgnoreFallback()) {
                return RabbitResult.notFound(action.getOriginUrl());
            }
        }

        // Normal navigation or fallback navigation all handled here.
        Navigator navigator = mNavigators.get(action.getTargetType());
        if (navigator == null) {
            if (notFound) {
                // need to be handled by fallback.
                // fallback handler isn't set.
                return RabbitResult.notFound(action.getOriginUrl());
            } else {
                return RabbitResult.error("Navigator not found for type " + action.getTargetType());
            }
        }
        return navigator.perform(action);
    }
}
