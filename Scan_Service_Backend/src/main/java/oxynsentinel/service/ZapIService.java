package oxynsentinel.service;

public interface ZapIService {
    public void runZapScan(String targetUrl) ;
    public String getZapScanResultById(Long scanId);

}
