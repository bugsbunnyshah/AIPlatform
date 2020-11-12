package io.bugsbunny.test.components;

import org.junit.jupiter.api.BeforeEach;

import javax.inject.Inject;

public abstract class BaseTest
{
    @Inject
    private SecurityTokenMockComponent securityTokenMockComponent;

    @BeforeEach
    public void setUp() throws Exception
    {
        this.securityTokenMockComponent.start();
    }
}
