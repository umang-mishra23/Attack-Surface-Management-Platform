export type Severity = "CRITICAL" | "HIGH" | "MEDIUM" | "LOW" | "INFORMATIONAL";
export type ScanStatus = "QUEUED" | "RUNNING" | "COMPLETED" | "FAILED";

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  company?: string;
  role: string;
  apiKey: string;
}

export interface ScanSummary {
  id: string;
  domain: string;
  scanType: "QUICK" | "STANDARD" | "DEEP";
  priority: "LOW" | "NORMAL" | "HIGH";
  status: ScanStatus;
  progress: number;
  riskScore: number;
  createdAt: string;
  completedAt?: string;
}

export interface DashboardStats {
  totalDomains: number;
  totalScans: number;
  totalAssets: number;
  aliveHosts: number;
  criticalFindings: number;
  highFindings: number;
  mediumFindings: number;
  lowFindings: number;
  lastScanTime?: string;
  successRate: number;
  monthlyTrend: Array<Record<string, unknown>>;
  severityDistribution: Record<string, number>;
  technologyDistribution: Record<string, number>;
  riskTrend: Array<Record<string, unknown>>;
}

export interface Asset {
  id: string;
  rootDomain: string;
  subdomain: string;
  ipAddress: string;
  status: string;
  responseCode?: number;
  pageTitle?: string;
  technologies: string[];
  riskScore: number;
  lastSeen: string;
}

export interface Finding {
  id: string;
  severity: Severity;
  status: string;
  name: string;
  description: string;
  affectedUrl: string;
  matchedTemplate?: string;
  cve?: string;
  cvss?: number;
  recommendation: string;
  dateFound: string;
}

export interface Notification {
  id: string;
  title: string;
  message: string;
  type: string;
  read: boolean;
  createdAt: string;
}
