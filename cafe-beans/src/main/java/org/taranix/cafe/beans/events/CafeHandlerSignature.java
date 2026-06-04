package org.taranix.cafe.beans.events;

import lombok.Builder;
import lombok.Getter;
import org.taranix.cafe.beans.metadata.CafeMethod;

import java.util.Optional;

@Getter
@Builder
public final class CafeHandlerSignature {

    private final CafeMethod handler;

    private final Optional<Object> instance;

}

