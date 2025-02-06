class WaterMark {
    //声明私有字段
    #element;
    #text;
    #settings;

    //声明私有字段
    constructor(element, text, settings = {}) {
        this.#element = element || document.documentElement;
        this.#text = text;
        this.#settings = {
            "offsetX": settings.offsetX || 20,                     // 水印起始位置x轴坐标
            "offsetY": settings.offsetY || 20,                     // 水印起始位置y轴坐标
            "spacingX": settings.spacingX || 150,                // 水印x轴间隔
            "spacingY": settings.spacingY || 100,                // 水印y轴间隔
            "fontSize": settings.fontSize || '22px',    //字体大小
            "fontFamily": settings.fontFamily || window.getComputedStyle(this.#element).fontFamily,
            "opacity": settings.opacity || 0.3,        // 水印透明度
            "rotateAngle": settings.rotateAngle || 20,           // 水印倾斜度
            "fontColor": settings.fontColor || 'grey'
        };
    }

    //创建水印
    create(remarkTime = false) {
        window.onload = () => {
            this.#buildWaterMark(remarkTime);
        };
        window.onresize = () => {
            this.#buildWaterMark(remarkTime);
        };
    }

    // 私有方法：生成水印
    #buildWaterMark(remarkTime = false) {
        // 清除已有的水印
        let watermarks = this.#element.getElementsByClassName("watermark");
        for (let watermark of watermarks) {
            watermark.remove();
        }
        // 获取当前页面的宽度和高度
        let page_width = this.#element.scrollWidth - this.#settings.offsetX;
        let page_height = this.#element.scrollHeight - this.#settings.offsetY;

        let fontSize = Number.parseInt(this.#settings.fontSize.replace('px', ''));
        let width = fontSize * this.#text.length;
        let height = fontSize;

        // 计算水印的列数和行数
        let cols = parseInt(page_width / (this.#settings.spacingX + width)) + 1;
        let rows = parseInt(page_height / (this.#settings.spacingY + height)) + 1;

        let x;
        let y;

        // 循环生成水印
        for (let i = 0; i < rows; i++) {
            for (let j = 0; j < cols; j++) {
                x = this.#settings.offsetX + (this.#settings.spacingX + width) * j;
                y = this.#settings.offsetY + (this.#settings.spacingY + height) * i;
                const watermark = document.createElement("span");
                watermark.className = 'watermark';
                watermark.innerHTML = this.#text + (remarkTime ? `<br>${this.#getNowFormatDay()}` : '');
                watermark.style.transform = "rotate(-" + this.#settings.rotateAngle.toString() + "deg)"; // 旋转
                watermark.style.position = "absolute";
                watermark.style.left = x + 'px';
                watermark.style.top = y + 'px';
                watermark.style.overflow = "hidden";
                watermark.style.zIndex = "9999";
                watermark.style.pointerEvents = "none";
                watermark.style.opacity = this.#settings.opacity;
                watermark.style.fontSize = this.#settings.fontSize;
                watermark.style.fontFamily = this.#settings.fontFamily
                watermark.style.textAlign = "center";
                watermark.style.color = this.#settings.fontColor;
                watermark.style.whiteSpace = 'nowrap'
                this.#element.appendChild(watermark);
            }
        }
    }

    // 获取当前时间
    #getNowFormatDay(nowDate = null) {
        let char = "-";
        if (nowDate == null) {
            nowDate = new Date();
        }
        let day = nowDate.getDate();
        let month = nowDate.getMonth() + 1; // 月份从0开始，所以要加1
        let year = nowDate.getFullYear();
        return year + char + this.#completeDate(month) + char + this.#completeDate(day);
    }

    // 补全日期，确保是两位数
    #completeDate(value) {
        return value < 10 ? "0" + value : value;
    }

}