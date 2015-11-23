from libcloud.compute.types import Provider
from libcloud.compute.providers import get_driver

auth_username = 'INF4410-05'
auth_password = 'KftpQvG49'
auth_url = 'http://pingouin.info.polymtl.ca:5000'
project_name = 'INF4410-05-projet'
region_name = ''

provider = get_driver(Provider.OPENSTACK)
conn = provider(auth_username,
                auth_password,
                ex_force_auth_url=auth_url,
                ex_force_auth_version='2.0_password',
                ex_tenant_name=project_name,
                ex_force_service_region=region_name)