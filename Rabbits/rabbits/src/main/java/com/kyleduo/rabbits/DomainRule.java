package com.kyleduo.rabbits;

import android.net.Uri;

/**
 * Rule for Domain.
 *
 * Created by kyle on 11/02/2018.
 */

public class DomainRule extends RuleImpl {
    DomainRule() {
    }

    @Override
    public boolean valid(Uri uri) {
        return valid(uri.getAuthority());
    }
}
