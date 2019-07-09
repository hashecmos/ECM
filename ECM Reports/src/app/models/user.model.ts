import { Role } from './role.model';

export class User {
  id: any;
  userName: string;
  fulName: string;
  title: string;
  mail: string;
  EmpNo: any;
  orgCode: any;
  appRole: string;

  roles: Role[];
  delegates: any[];
}
