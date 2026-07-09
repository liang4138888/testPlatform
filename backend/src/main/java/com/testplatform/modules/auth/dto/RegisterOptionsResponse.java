package com.testplatform.modules.auth.dto;

import java.util.List;

import com.testplatform.modules.organization.dto.OrganizationResponse;

public class RegisterOptionsResponse {
    private List<OrganizationResponse> organizations;

    public List<OrganizationResponse> getOrganizations() { return organizations; }
    public void setOrganizations(List<OrganizationResponse> organizations) { this.organizations = organizations; }
}
