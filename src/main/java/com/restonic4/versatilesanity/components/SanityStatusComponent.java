package com.restonic4.versatilesanity.components;

import dev.onyxstudios.cca.api.v3.component.Component;

public interface  SanityStatusComponent extends Component {
    int getSanityStatus();

    float getSanityPercentage();

    void setSanityStatus(int value);

    void decrementSanityStatus(int amount);

    void incrementSanityStatus(int amount);

    void sync();
}