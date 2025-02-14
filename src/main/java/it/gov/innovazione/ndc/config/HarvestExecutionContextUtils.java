package it.gov.innovazione.ndc.config;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class HarvestExecutionContextUtils {

    private static final ThreadLocal<HarvestExecutionContext> CONTEXT_HOLDER = new ThreadLocal<>();

    public static HarvestExecutionContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    public static void setContext(HarvestExecutionContext context) {
        CONTEXT_HOLDER.set(context);
    }
}
